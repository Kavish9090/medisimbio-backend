package com.medisimbio.backend.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.patient.dto.CreatePatientRequestDto;
import com.medisimbio.backend.patient.dto.UpdatePatientRequestDto;
import com.medisimbio.backend.patient.entity.Gender;
import com.medisimbio.backend.patient.repository.PatientRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private static final String TEST_AUTH_USER_1 = "auth_user_patient_001";
    private static final String TEST_AUTH_USER_2 = "auth_user_patient_002";

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM diagnoses");
        jdbcTemplate.execute("DELETE FROM vitals");
        jdbcTemplate.execute("DELETE FROM encounters");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM med_ids");
        jdbcTemplate.execute("DELETE FROM patients");
    }

    private CreatePatientRequestDto createValidPatientDto(String email, String mobile) {
        CreatePatientRequestDto dto = new CreatePatientRequestDto(
                "Sarah Connor",
                email,
                mobile,
                LocalDate.of(1985, 4, 12),
                Gender.FEMALE
        );
        dto.setBloodGroup("O+");
        dto.setAllergies("Sulfa drugs");
        dto.setEmergencyContactName("John Connor");
        dto.setEmergencyContactRelationship("Son");
        dto.setEmergencyContactMobile("+919876543299");
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/patients: Successfully creates new patient profile")
    void createPatient_success() throws Exception {
        CreatePatientRequestDto dto = createValidPatientDto("sarah.connor@example.com", "+919876543201");

        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.authUserId", is(TEST_AUTH_USER_1)))
                .andExpect(jsonPath("$.data.fullName", is("Sarah Connor")))
                .andExpect(jsonPath("$.data.email", is("sarah.connor@example.com")))
                .andExpect(jsonPath("$.data.mobileNumber", is("+919876543201")))
                .andExpect(jsonPath("$.data.gender", is("FEMALE")))
                .andExpect(jsonPath("$.data.bloodGroup", is("O+")))
                .andExpect(jsonPath("$.data.profileCompleted", is(false)));
    }

    @Test
    @DisplayName("POST /api/v1/patients: 401 Unauthorized when request lacks authentication")
    void createPatient_unauthorized() throws Exception {
        CreatePatientRequestDto dto = createValidPatientDto("unauth@example.com", "+919876543202");

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("POST /api/v1/patients: 400 Bad Request when payload violates validation constraints")
    void createPatient_validationFailure() throws Exception {
        CreatePatientRequestDto invalidDto = new CreatePatientRequestDto(
                "", // Blank name
                "invalid-email", // Invalid email
                "abc", // Invalid mobile format
                LocalDate.now().plusDays(1), // Future birthdate
                null // Null gender
        );

        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("POST /api/v1/patients: 409 Conflict when auth user already has a registered profile")
    void createPatient_duplicateAuthUser() throws Exception {
        CreatePatientRequestDto dto1 = createValidPatientDto("patient1@example.com", "+919876543211");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        CreatePatientRequestDto dto2 = createValidPatientDto("patient2@example.com", "+919876543212");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1) // Same auth user
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("POST /api/v1/patients: 409 Conflict when email is already registered by another user")
    void createPatient_duplicateEmail() throws Exception {
        CreatePatientRequestDto dto1 = createValidPatientDto("duplicate@example.com", "+919876543221");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        CreatePatientRequestDto dto2 = createValidPatientDto("duplicate@example.com", "+919876543222");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("GET /api/v1/patients/me: Retrieves own profile for authenticated patient")
    void getCurrentPatient_success() throws Exception {
        CreatePatientRequestDto dto = createValidPatientDto("me.test@example.com", "+919876543231");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/patients/me")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("Sarah Connor")))
                .andExpect(jsonPath("$.data.email", is("me.test@example.com")));
    }

    @Test
    @DisplayName("GET /api/v1/patients/me: 404 Not Found when profile does not exist for auth user")
    void getCurrentPatient_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "non_existent_user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("RESOURCE_NOT_FOUND")));
    }

    @Test
    @DisplayName("PATCH /api/v1/patients/me: Partially updates profile attributes")
    void updateCurrentPatient_success() throws Exception {
        CreatePatientRequestDto dto = createValidPatientDto("patch.test@example.com", "+919876543241");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        UpdatePatientRequestDto updateDto = new UpdatePatientRequestDto();
        updateDto.setBloodGroup("AB+");
        updateDto.setProfileCompleted(true);
        updateDto.setEmergencyContactName("Kyle Reese");

        mockMvc.perform(patch("/api/v1/patients/me")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bloodGroup", is("AB+")))
                .andExpect(jsonPath("$.data.profileCompleted", is(true)))
                .andExpect(jsonPath("$.data.emergencyContactName", is("Kyle Reese")));
    }

    @Test
    @DisplayName("Med-ID Lifecycle: POST generate -> GET retrieve -> Idempotency check")
    void medIdLifecycle_success() throws Exception {
        // 1. Create Patient Profile
        CreatePatientRequestDto dto = createValidPatientDto("medid.test@example.com", "+919876543251");
        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // 2. Initial Med-ID retrieval before generation should return 404
        mockMvc.perform(get("/api/v1/patients/me/med-id")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1))
                .andExpect(status().isNotFound());

        // 3. Generate Med-ID
        String firstMedIdResponse = mockMvc.perform(post("/api/v1/patients/me/med-id")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.medId", matchesPattern("^MD-[A-Z0-9]{4}-[A-Z0-9]{4}$")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.qrToken", notNullValue()))
                .andExpect(jsonPath("$.data.patientName", is("Sarah Connor")))
                .andReturn().getResponse().getContentAsString();

        String generatedMedId = objectMapper.readTree(firstMedIdResponse).path("data").path("medId").asText();

        // 4. Retrieve Med-ID via GET
        mockMvc.perform(get("/api/v1/patients/me/med-id")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.medId", is(generatedMedId)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        // 5. Patient profile GET /me should now reflect the assigned medId
        mockMvc.perform(get("/api/v1/patients/me")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.medId", is(generatedMedId)));

        // 6. Calling POST /api/v1/patients/me/med-id again must be idempotent and return the exact same Med-ID
        mockMvc.perform(post("/api/v1/patients/me/med-id")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.medId", is(generatedMedId)));
    }
}
