package com.medisimbio.backend.encounter.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.encounter.dto.*;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import com.medisimbio.backend.encounter.service.EncounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * REST Controller for Clinical Encounter management and consultation lifecycles.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Clinical Encounters", description = "Endpoints for managing clinical encounters and consultation sessions")
@SecurityRequirement(name = "bearerAuth")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @PostMapping("/encounters")
    @Operation(summary = "Create Clinical Encounter", description = "Initiates a new clinical encounter associated with a scheduled appointment")
    public ResponseEntity<ApiResponse<EncounterResponseDto>> createEncounter(
            @Valid @RequestBody CreateEncounterRequestDto requestDto) {
        EncounterResponseDto response = encounterService.createEncounter(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clinical encounter created successfully", response));
    }

    @GetMapping("/encounters/{encounterId}")
    @Operation(summary = "Get Encounter by ID", description = "Retrieves clinical encounter details. Enforces patient and provider IDOR access control.")
    public ResponseEntity<ApiResponse<EncounterResponseDto>> getEncounterById(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId) {
        EncounterResponseDto response = encounterService.getEncounterById(encounterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/encounters/{encounterId}")
    @Operation(summary = "Update Clinical Notes", description = "Updates clinical notes, chief complaint, diagnosis summary, and treatment plan for an active encounter")
    public ResponseEntity<ApiResponse<EncounterResponseDto>> updateEncounter(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId,
            @Valid @RequestBody UpdateEncounterRequestDto requestDto) {
        EncounterResponseDto response = encounterService.updateEncounter(encounterId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/encounters/{encounterId}/start")
    @Operation(summary = "Start Clinical Encounter", description = "Transitions encounter lifecycle from OPEN to IN_PROGRESS")
    public ResponseEntity<ApiResponse<EncounterResponseDto>> startEncounter(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId) {
        EncounterResponseDto response = encounterService.startEncounter(encounterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/encounters/{encounterId}/complete")
    @Operation(summary = "Complete Clinical Encounter", description = "Finalizes encounter (COMPLETED) and synchronizes linked appointment status to COMPLETED")
    public ResponseEntity<ApiResponse<EncounterResponseDto>> completeEncounter(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId) {
        EncounterResponseDto response = encounterService.completeEncounter(encounterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/encounters/{encounterId}/cancel")
    @Operation(summary = "Cancel Clinical Encounter", description = "Cancels active encounter session (CANCELLED)")
    public ResponseEntity<ApiResponse<EncounterResponseDto>> cancelEncounter(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId,
            @RequestParam(required = false) String reason) {
        EncounterResponseDto response = encounterService.cancelEncounter(encounterId, reason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/patients/me/encounters")
    @Operation(summary = "Get Patient Encounters", description = "Retrieves paginated list of clinical encounters for authenticated patient")
    public ResponseEntity<ApiResponse<PageResponse<EncounterSummaryDto>>> getPatientEncounters(
            @RequestParam(required = false) EncounterStatus status,
            @RequestParam(required = false) EncounterType encounterType,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<EncounterSummaryDto> response = encounterService.getPatientEncounters(status, encounterType, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/providers/{providerId}/encounters")
    @Operation(summary = "Get Provider Encounters", description = "Retrieves paginated list of clinical encounters for a healthcare provider (Authorized provider only)")
    public ResponseEntity<ApiResponse<PageResponse<EncounterSummaryDto>>> getProviderEncounters(
            @Parameter(description = "Provider UUID") @PathVariable UUID providerId,
            @RequestParam(required = false) EncounterStatus status,
            @RequestParam(required = false) EncounterType encounterType,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<EncounterSummaryDto> response = encounterService.getProviderEncounters(providerId, status, encounterType, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
