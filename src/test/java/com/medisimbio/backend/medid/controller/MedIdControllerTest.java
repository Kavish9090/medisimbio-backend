package com.medisimbio.backend.medid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.patient.dto.CreatePatientRequestDto;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MedIdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM diagnoses");
        jdbcTemplate.execute("DELETE FROM vitals");
        jdbcTemplate.execute("DELETE FROM encounters");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM med_ids");
        jdbcTemplate.execute("DELETE FROM patients");
    }

    @Test
    @DisplayName("GET /api/v1/med-id/verify/{medId}: Verifies valid Med-ID and returns masked identity")
    void verifyMedId_success() throws Exception {
        // 1. Create Patient & Med-ID
        CreatePatientRequestDto dto = new CreatePatientRequestDto(
                "Alexander Hamilton",
                "alex.h@example.com",
                "+919876543290",
                LocalDate.of(1992, 1, 11),
                Gender.MALE
        );

        mockMvc.perform(post("/api/v1/patients")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "auth_user_alex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String medIdJson = mockMvc.perform(post("/api/v1/patients/me/med-id")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "auth_user_alex"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String generatedMedId = objectMapper.readTree(medIdJson).path("data").path("medId").asText();

        // 2. Verify Med-ID (unauthenticated public verification endpoint)
        mockMvc.perform(get("/api/v1/med-id/verify/" + generatedMedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.medId", is(generatedMedId)))
                .andExpect(jsonPath("$.data.valid", is(true)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.maskedPatientName", is("A*******r H******n")));
    }

    @Test
    @DisplayName("GET /api/v1/med-id/verify/{medId}: 400 Bad Request for malformed Med-ID format")
    void verifyMedId_invalidFormat() throws Exception {
        mockMvc.perform(get("/api/v1/med-id/verify/INVALID_MED_ID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("GET /api/v1/med-id/verify/{medId}: 404 Not Found for non-existent Med-ID")
    void verifyMedId_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/med-id/verify/MD-9999-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("RESOURCE_NOT_FOUND")));
    }
}
