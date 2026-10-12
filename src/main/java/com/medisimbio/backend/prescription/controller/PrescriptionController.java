package com.medisimbio.backend.prescription.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.prescription.dto.*;
import com.medisimbio.backend.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Prescriptions", description = "Endpoints for electronic prescriptions, item management, and lifecycle state transitions")
@SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/encounters/{encounterId}/prescriptions")
    @Operation(summary = "Create Prescription", description = "Creates a new electronic prescription (DRAFT or FINALIZED) for an eligible clinical encounter")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> createPrescription(
            @PathVariable UUID encounterId,
            @Valid @RequestBody CreatePrescriptionRequestDto requestDto) {
        PrescriptionResponseDto response = prescriptionService.createPrescription(encounterId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    @Operation(summary = "Get Prescription by ID", description = "Retrieves full prescription details including all structured medicine items")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> getPrescription(
            @PathVariable UUID prescriptionId) {
        PrescriptionResponseDto response = prescriptionService.getPrescriptionById(prescriptionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/encounters/{encounterId}/prescriptions")
    @Operation(summary = "List Encounter Prescriptions", description = "Retrieves paginated prescriptions authored for a specific clinical encounter")
    public ResponseEntity<ApiResponse<Page<PrescriptionSummaryDto>>> getPrescriptionsByEncounter(
            @PathVariable UUID encounterId,
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10") @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<PrescriptionSummaryDto> response = prescriptionService.getPrescriptionsByEncounter(
                encounterId, PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "prescribedAt")));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/patients/me/prescriptions")
    @Operation(summary = "List My Prescriptions", description = "Retrieves paginated finalized/superseded/cancelled prescriptions for authenticated patient")
    public ResponseEntity<ApiResponse<Page<PrescriptionSummaryDto>>> getMyPrescriptions(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10") @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<PrescriptionSummaryDto> response = prescriptionService.getPatientPrescriptions(
                PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "prescribedAt")));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/prescriptions/{prescriptionId}")
    @Operation(summary = "Update Draft Prescription", description = "Modifies instructions or items on a DRAFT prescription prior to finalization")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> updateDraftPrescription(
            @PathVariable UUID prescriptionId,
            @Valid @RequestBody UpdatePrescriptionRequestDto requestDto) {
        PrescriptionResponseDto response = prescriptionService.updateDraftPrescription(prescriptionId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/prescriptions/{prescriptionId}/finalize")
    @Operation(summary = "Finalize Prescription", description = "Finalizes a DRAFT prescription, making it immutable and visible to patient/timeline")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> finalizePrescription(
            @PathVariable UUID prescriptionId) {
        PrescriptionResponseDto response = prescriptionService.finalizePrescription(prescriptionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/prescriptions/{prescriptionId}/cancel")
    @Operation(summary = "Cancel Prescription", description = "Cancels a prescription with mandatory clinical reason while preserving audit records")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> cancelPrescription(
            @PathVariable UUID prescriptionId,
            @Valid @RequestBody CancelPrescriptionRequestDto requestDto) {
        PrescriptionResponseDto response = prescriptionService.cancelPrescription(prescriptionId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/prescriptions/{prescriptionId}/replacements")
    @Operation(summary = "Replace/Supersede Prescription", description = "Atomically supersedes a finalized prescription and creates an amended version")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> replacePrescription(
            @PathVariable UUID prescriptionId,
            @Valid @RequestBody ReplacePrescriptionRequestDto requestDto) {
        PrescriptionResponseDto response = prescriptionService.replacePrescription(prescriptionId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
