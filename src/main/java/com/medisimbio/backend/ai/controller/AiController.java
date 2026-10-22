package com.medisimbio.backend.ai.controller;

import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckRequestDto;
import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckResponseDto;
import com.medisimbio.backend.ai.dto.AiTriageRequestDto;
import com.medisimbio.backend.ai.dto.AiTriageResponseDto;
import com.medisimbio.backend.ai.service.AiIntegrationService;
import com.medisimbio.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Assistant & Clinical Intelligence Controller.
 * Bridges Mobile and Web portal clients with the AI inference capabilities.
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Clinical Assistant", description = "AI symptom triage, clinical intake extraction, and prescription safety checks")
public class AiController {

    private final AiIntegrationService aiIntegrationService;

    public AiController(AiIntegrationService aiIntegrationService) {
        this.aiIntegrationService = aiIntegrationService;
    }

    @PostMapping("/triage")
    @Operation(summary = "Perform symptom triage", description = "Analyzes patient symptoms, calculates triage urgency, and suggests relevant clinical specialty")
    public ResponseEntity<ApiResponse<AiTriageResponseDto>> performTriage(
            @Valid @RequestBody AiTriageRequestDto requestDto) {
        AiTriageResponseDto response = aiIntegrationService.performTriage(requestDto);
        return ResponseEntity.ok(ApiResponse.success("AI triage evaluation completed", response));
    }

    @PostMapping("/prescriptions/safety-check")
    @Operation(summary = "Prescription safety screening", description = "Validates candidate medications against allergies, active conditions, and drug interactions")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'PATIENT')")
    public ResponseEntity<ApiResponse<AiPrescriptionSafetyCheckResponseDto>> checkPrescriptionSafety(
            @Valid @RequestBody AiPrescriptionSafetyCheckRequestDto requestDto) {
        AiPrescriptionSafetyCheckResponseDto response = aiIntegrationService.checkPrescriptionSafety(requestDto);
        return ResponseEntity.ok(ApiResponse.success("Prescription safety check completed", response));
    }
}
