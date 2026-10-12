package com.medisimbio.backend.provider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.hospital.dto.CreateHospitalRequestDto;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.provider.dto.AssignHospitalRequestDto;
import com.medisimbio.backend.provider.dto.AssignSpecialtyRequestDto;
import com.medisimbio.backend.provider.dto.CreateProviderRequestDto;
import com.medisimbio.backend.provider.dto.UpdateProviderRequestDto;
import com.medisimbio.backend.security.DevAuthenticationFilter;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import com.medisimbio.backend.specialty.repository.SpecialtyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    private static final String TEST_ADMIN_USER = "auth_admin_prov_001";

    private CreateProviderRequestDto createValidProviderDto(String regNum, String email, String mobile) {
        CreateProviderRequestDto dto = new CreateProviderRequestDto();
        dto.setFullName("Dr. Rajesh Sharma");
        dto.setProfessionalRegistrationNumber(regNum);
        dto.setQualification("MBBS, MD - Cardiology");
        dto.setExperienceYears(12);
        dto.setEmail(email);
        dto.setMobileNumber(mobile);
        dto.setProfilePhotoUrl("https://example.com/photos/dr-rajesh.jpg");
        return dto;
    }

    private String createHospitalHelper(String name, String regId, String city) throws Exception {
        CreateHospitalRequestDto dto = new CreateHospitalRequestDto();
        dto.setName(name);
        dto.setHospitalType(HospitalType.PRIVATE);
        dto.setRegistrationIdentifier(regId);
        dto.setPhone("+911122334455");
        dto.setAddress("Healthcare Way");
        dto.setCity(city);
        dto.setState("Delhi");
        dto.setPostalCode("110001");

        String response = mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asText();
    }

    @Test
    @DisplayName("POST /api/v1/providers: Successfully registers provider with initial specialties")
    void createProvider_success() throws Exception {
        SpecialtyEntity cardiology = specialtyRepository.findByCode("CARDIOLOGY").orElseThrow();

        CreateProviderRequestDto dto = createValidProviderDto("MCI-2015-89412", "rajesh.cardio@example.com", "+919811223344");
        dto.setSpecialtyIds(Set.of(cardiology.getId()));

        mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.fullName", is("Dr. Rajesh Sharma")))
                .andExpect(jsonPath("$.data.professionalRegistrationNumber", is("MCI-2015-89412")))
                .andExpect(jsonPath("$.data.specialties", hasSize(1)))
                .andExpect(jsonPath("$.data.specialties[0].code", is("CARDIOLOGY")));
    }

    @Test
    @DisplayName("POST /api/v1/providers: 409 Conflict when registration number is duplicated")
    void createProvider_duplicateRegNum() throws Exception {
        CreateProviderRequestDto dto1 = createValidProviderDto("MCI-99999", "doc1@example.com", "+919811111111");
        CreateProviderRequestDto dto2 = createValidProviderDto("MCI-99999", "doc2@example.com", "+919822222222");

        mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("POST /api/v1/providers: 401 Unauthorized when unauthenticated")
    void createProvider_unauthorized() throws Exception {
        CreateProviderRequestDto dto = createValidProviderDto("MCI-UNAUTH", "unauth.doc@example.com", "+919833333333");

        mockMvc.perform(post("/api/v1/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("PATCH /api/v1/providers/{id}: Updates provider details")
    void updateProvider_success() throws Exception {
        CreateProviderRequestDto dto = createValidProviderDto("MCI-UPDATE-01", "update.doc@example.com", "+919844444444");

        String responseJson = mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String providerId = objectMapper.readTree(responseJson).path("data").path("id").asText();

        UpdateProviderRequestDto updateDto = new UpdateProviderRequestDto();
        updateDto.setExperienceYears(15);
        updateDto.setQualification("MBBS, MD, DM, FACC (USA)");

        mockMvc.perform(patch("/api/v1/providers/" + providerId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.experienceYears", is(15)))
                .andExpect(jsonPath("$.data.qualification", is("MBBS, MD, DM, FACC (USA)")));
    }

    @Test
    @DisplayName("Specialty & Hospital Affiliation Endpoints: Assign, List, and Delete")
    void providerAffiliations_lifecycle() throws Exception {
        // 1. Create Provider
        CreateProviderRequestDto provDto = createValidProviderDto("MCI-AFFIL-01", "affil.doc@example.com", "+919855555555");
        String provJson = mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(provDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String providerId = objectMapper.readTree(provJson).path("data").path("id").asText();

        // 2. Assign Specialty
        SpecialtyEntity neurology = specialtyRepository.findByCode("NEUROLOGY").orElseThrow();
        AssignSpecialtyRequestDto specDto = new AssignSpecialtyRequestDto(Set.of(neurology.getId()));
        mockMvc.perform(post("/api/v1/providers/" + providerId + "/specialties")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.specialties", hasSize(1)))
                .andExpect(jsonPath("$.data.specialties[0].code", is("NEUROLOGY")));

        // 3. List Specialties for Provider (Public)
        mockMvc.perform(get("/api/v1/providers/" + providerId + "/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code", is("NEUROLOGY")));

        // 4. Create Hospital & Affiliate
        String hospitalId = createHospitalHelper("Fortis Escorts Heart Institute", "HOSP-FORTIS-ESC-01", "New Delhi");
        AssignHospitalRequestDto hospDto = new AssignHospitalRequestDto(
                UUID.fromString(hospitalId),
                "Neuro-Cardiology Division",
                new BigDecimal("1200.00"),
                true
        );

        mockMvc.perform(post("/api/v1/providers/" + providerId + "/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hospDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.department", is("Neuro-Cardiology Division")))
                .andExpect(jsonPath("$.data.consultationFee", is(1200.00)));

        // 5. List Hospitals for Provider (Public)
        mockMvc.perform(get("/api/v1/providers/" + providerId + "/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].hospital.name", is("Fortis Escorts Heart Institute")));

        // 6. List Doctors for Hospital (Public)
        mockMvc.perform(get("/api/v1/hospitals/" + hospitalId + "/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].fullName", is("Dr. Rajesh Sharma")));

        // 7. Remove Specialty
        mockMvc.perform(delete("/api/v1/providers/" + providerId + "/specialties/" + neurology.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/providers/" + providerId + "/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));

        // 8. Remove Hospital Affiliation
        mockMvc.perform(delete("/api/v1/providers/" + providerId + "/hospitals/" + hospitalId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/providers/" + providerId + "/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/providers: Dynamic search filtering by name, qualification, registration")
    void getProviders_searchFilters() throws Exception {
        CreateProviderRequestDto drAmit = createValidProviderDto("MCI-AMIT-01", "amit@example.com", "+919877777771");
        drAmit.setFullName("Dr. Amit Verma");
        drAmit.setQualification("MBBS, MS - Orthopedics");

        CreateProviderRequestDto drPriya = createValidProviderDto("MCI-PRIYA-02", "priya@example.com", "+919877777772");
        drPriya.setFullName("Dr. Priya Nair");
        drPriya.setQualification("MBBS, MD - Dermatology");

        mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drAmit)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/providers")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drPriya)))
                .andExpect(status().isCreated());

        // Search by keyword "Orthopedics"
        mockMvc.perform(get("/api/v1/providers?search=Orthopedics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].fullName", is("Dr. Amit Verma")));

        // Search by name "Priya"
        mockMvc.perform(get("/api/v1/providers?search=Priya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].fullName", is("Dr. Priya Nair")));
    }
}
