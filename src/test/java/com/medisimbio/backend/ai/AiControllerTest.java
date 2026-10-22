package com.medisimbio.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckRequestDto;
import com.medisimbio.backend.ai.dto.AiTriageRequestDto;
import com.medisimbio.backend.security.DevAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AI Triage: Successful evaluation with valid symptom intake")
    void shouldPerformTriageSuccessfully() throws Exception {
        AiTriageRequestDto request = new AiTriageRequestDto(
                28,
                "MALE",
                List.of("Severe headache", "Sensitivity to light", "Nausea"),
                "Throbbing headache on left side since morning with nausea.",
                "1 day",
                7
        );

        mockMvc.perform(post("/api/v1/ai/triage")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "test-user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.triageLevel").value("URGENT"))
                .andExpect(jsonPath("$.data.recommendedSpecialtyCode").isNotEmpty())
                .andExpect(jsonPath("$.data.clinicalIntakeSummary").isNotEmpty());
    }

    @Test
    @DisplayName("AI Triage: Returns 400 Bad Request when symptoms list is empty")
    void shouldRejectTriageWithEmptySymptoms() throws Exception {
        AiTriageRequestDto request = new AiTriageRequestDto(
                28,
                "MALE",
                List.of(),
                "Some description",
                "1 day",
                3
        );

        mockMvc.perform(post("/api/v1/ai/triage")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "test-user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("AI Prescription Safety: Detects allergy contraindication")
    void shouldDetectPrescriptionAllergyAlert() throws Exception {
        AiPrescriptionSafetyCheckRequestDto request = new AiPrescriptionSafetyCheckRequestDto(
                UUID.randomUUID(),
                List.of("Amoxicillin 500mg"),
                List.of("Paracetamol 650mg"),
                List.of("Amoxicillin"),
                List.of("Upper Respiratory Tract Infection")
        );

        mockMvc.perform(post("/api/v1/ai/prescriptions/safety-check")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "doc-test-1")
                        .header(DevAuthenticationFilter.HEADER_USER_ROLE, "DOCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.safetyLevel").value("CONTRAINDICATED"))
                .andExpect(jsonPath("$.data.allergyAlerts", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("AI Prescription Safety: Passes safe medications when no conflicts exist")
    void shouldPassSafePrescriptionVerification() throws Exception {
        AiPrescriptionSafetyCheckRequestDto request = new AiPrescriptionSafetyCheckRequestDto(
                UUID.randomUUID(),
                List.of("Azithromycin 500mg"),
                List.of("Cetirizine 10mg"),
                List.of("Penicillin"),
                List.of("Bronchitis")
        );

        mockMvc.perform(post("/api/v1/ai/prescriptions/safety-check")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "doc-test-1")
                        .header(DevAuthenticationFilter.HEADER_USER_ROLE, "DOCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.safetyLevel").value("SAFE"))
                .andExpect(jsonPath("$.data.allergyAlerts", hasSize(0)));
    }
}
