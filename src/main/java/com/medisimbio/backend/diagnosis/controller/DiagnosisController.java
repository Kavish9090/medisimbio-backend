package com.medisimbio.backend.diagnosis.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.diagnosis.dto.CreateDiagnosisRequestDto;
import com.medisimbio.backend.diagnosis.dto.DiagnosisResponseDto;
import com.medisimbio.backend.diagnosis.dto.UpdateDiagnosisRequestDto;
import com.medisimbio.backend.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for clinical diagnosis management.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Clinical Diagnoses", description = "Endpoints for recording and managing clinical diagnoses")
@SecurityRequirement(name = "bearerAuth")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/encounters/{encounterId}/diagnoses")
    @Operation(summary = "Record Clinical Diagnosis", description = "Adds a provider-entered clinical diagnosis (primary, secondary, provisional, rule-out) to an encounter")
    public ResponseEntity<ApiResponse<DiagnosisResponseDto>> createDiagnosis(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId,
            @Valid @RequestBody CreateDiagnosisRequestDto requestDto) {
        DiagnosisResponseDto response = diagnosisService.createDiagnosis(encounterId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clinical diagnosis recorded successfully", response));
    }

    @GetMapping("/encounters/{encounterId}/diagnoses")
    @Operation(summary = "Get Diagnoses by Encounter", description = "Retrieves all clinical diagnoses recorded for an encounter session")
    public ResponseEntity<ApiResponse<List<DiagnosisResponseDto>>> getDiagnosesByEncounter(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId) {
        List<DiagnosisResponseDto> response = diagnosisService.getDiagnosesByEncounterId(encounterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/diagnoses/{diagnosisId}")
    @Operation(summary = "Update Clinical Diagnosis", description = "Modifies clinical status, ICD code, or observations on an existing diagnosis")
    public ResponseEntity<ApiResponse<DiagnosisResponseDto>> updateDiagnosis(
            @Parameter(description = "Diagnosis UUID") @PathVariable UUID diagnosisId,
            @Valid @RequestBody UpdateDiagnosisRequestDto requestDto) {
        DiagnosisResponseDto response = diagnosisService.updateDiagnosis(diagnosisId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
