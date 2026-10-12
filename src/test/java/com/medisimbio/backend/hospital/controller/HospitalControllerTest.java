package com.medisimbio.backend.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.hospital.dto.CreateHospitalRequestDto;
import com.medisimbio.backend.hospital.dto.UpdateHospitalRequestDto;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.security.DevAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HospitalRepository hospitalRepository;

    private static final String TEST_ADMIN_USER = "auth_admin_hosp_001";

    private CreateHospitalRequestDto createValidHospitalDto(String name, String regId, HospitalType type, String city) {
        CreateHospitalRequestDto dto = new CreateHospitalRequestDto();
        dto.setName(name);
        dto.setHospitalType(type);
        dto.setRegistrationIdentifier(regId);
        dto.setPhone("+911126588500");
        dto.setEmail("contact@" + city.toLowerCase().replace(" ", "") + "hospital.org");
        dto.setAddress("Main Healthcare Boulevard, Sector 10");
        dto.setCity(city);
        dto.setState("Delhi");
        dto.setPostalCode("110029");
        dto.setLatitude(28.5672);
        dto.setLongitude(77.2100);
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/hospitals: Successfully creates hospital when authenticated")
    void createHospital_success() throws Exception {
        CreateHospitalRequestDto dto = createValidHospitalDto(
                "All India Institute of Medical Sciences",
                "HOSP-AIIMS-001",
                HospitalType.GOVERNMENT,
                "New Delhi"
        );

        mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.name", is("All India Institute of Medical Sciences")))
                .andExpect(jsonPath("$.data.hospitalType", is("GOVERNMENT")))
                .andExpect(jsonPath("$.data.registrationIdentifier", is("HOSP-AIIMS-001")))
                .andExpect(jsonPath("$.data.city", is("New Delhi")))
                .andExpect(jsonPath("$.data.active", is(true)));
    }

    @Test
    @DisplayName("POST /api/v1/hospitals: 401 Unauthorized when unauthenticated")
    void createHospital_unauthorized() throws Exception {
        CreateHospitalRequestDto dto = createValidHospitalDto(
                "Apollo Hospitals",
                "HOSP-APOLLO-001",
                HospitalType.PRIVATE,
                "New Delhi"
        );

        mockMvc.perform(post("/api/v1/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("POST /api/v1/hospitals: 409 Conflict when registration identifier already exists")
    void createHospital_conflict() throws Exception {
        CreateHospitalRequestDto dto1 = createValidHospitalDto("Max Super Speciality", "HOSP-MAX-001", HospitalType.PRIVATE, "New Delhi");
        CreateHospitalRequestDto dto2 = createValidHospitalDto("Max Hospital Saket", "HOSP-MAX-001", HospitalType.PRIVATE, "New Delhi");

        mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("GET /api/v1/hospitals: Publicly accessible and supports filtering by city, type, and search")
    void getHospitals_filteringAndSearch() throws Exception {
        CreateHospitalRequestDto aiims = createValidHospitalDto("AIIMS Delhi", "HOSP-AIIMS-DEL", HospitalType.GOVERNMENT, "New Delhi");
        CreateHospitalRequestDto fortis = createValidHospitalDto("Fortis Memorial", "HOSP-FORTIS-GUR", HospitalType.PRIVATE, "Gurugram");

        mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aiims)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fortis)))
                .andExpect(status().isCreated());

        // Public Search by City
        mockMvc.perform(get("/api/v1/hospitals?city=Gurugram"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name", is("Fortis Memorial")));

        // Public Search by Type
        mockMvc.perform(get("/api/v1/hospitals?type=GOVERNMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name", is("AIIMS Delhi")));

        // Public Search by Query keyword
        mockMvc.perform(get("/api/v1/hospitals?search=Fortis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name", is("Fortis Memorial")));
    }

    @Test
    @DisplayName("PATCH /api/v1/hospitals/{id}: Updates hospital attributes")
    void updateHospital_success() throws Exception {
        CreateHospitalRequestDto dto = createValidHospitalDto("Medanta Hospital", "HOSP-MEDANTA-01", HospitalType.PRIVATE, "Gurugram");

        String responseJson = mockMvc.perform(post("/api/v1/hospitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String hospitalId = objectMapper.readTree(responseJson).path("data").path("id").asText();

        UpdateHospitalRequestDto updateDto = new UpdateHospitalRequestDto();
        updateDto.setPhone("+911244141414");
        updateDto.setName("Medanta The Medicity");

        mockMvc.perform(patch("/api/v1/hospitals/" + hospitalId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Medanta The Medicity")))
                .andExpect(jsonPath("$.data.phone", is("+911244141414")));
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/{id}: 404 when hospital does not exist")
    void getHospitalById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("RESOURCE_NOT_FOUND")));
    }
}
