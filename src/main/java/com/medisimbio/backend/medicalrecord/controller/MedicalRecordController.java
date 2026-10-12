package com.medisimbio.backend.medicalrecord.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.medicalrecord.dto.MedicalRecordOverviewDto;
import com.medisimbio.backend.medicalrecord.dto.MedicalTimelineResponseDto;
import com.medisimbio.backend.medicalrecord.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for accessing patient medical records and chronological clinical timelines.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Medical Records", description = "Endpoints for consolidated patient medical records and chronological event timelines")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/patients/me/medical-record")
    @Operation(summary = "Get Consolidated Medical Record", description = "Retrieves aggregated medical record overview for authenticated patient (encounters, active diagnoses, latest vitals)")
    public ResponseEntity<ApiResponse<MedicalRecordOverviewDto>> getPatientMedicalRecord() {
        MedicalRecordOverviewDto response = medicalRecordService.getPatientMedicalRecordOverview();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/patients/me/medical-record/timeline")
    @Operation(summary = "Get Medical Timeline", description = "Retrieves paginated chronological clinical events timeline for authenticated patient")
    public ResponseEntity<ApiResponse<MedicalTimelineResponseDto>> getPatientMedicalTimeline(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)", example = "20") @RequestParam(defaultValue = "20") int size) {
        MedicalTimelineResponseDto response = medicalRecordService.getPatientMedicalTimeline(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
