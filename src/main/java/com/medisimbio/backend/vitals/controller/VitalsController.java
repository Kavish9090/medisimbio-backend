package com.medisimbio.backend.vitals.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.vitals.dto.CreateVitalsRequestDto;
import com.medisimbio.backend.vitals.dto.VitalsResponseDto;
import com.medisimbio.backend.vitals.service.VitalsService;
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
 * REST Controller for physiological vitals management.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Physiological Vitals", description = "Endpoints for recording and retrieving encounter physiological vitals")
@SecurityRequirement(name = "bearerAuth")
public class VitalsController {

    private final VitalsService vitalsService;

    public VitalsController(VitalsService vitalsService) {
        this.vitalsService = vitalsService;
    }

    @PostMapping("/encounters/{encounterId}/vitals")
    @Operation(summary = "Record Vitals", description = "Records physiological vitals (temperature, heart rate, BP, SpO2, BMI) for an encounter")
    public ResponseEntity<ApiResponse<VitalsResponseDto>> createVitals(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId,
            @Valid @RequestBody CreateVitalsRequestDto requestDto) {
        VitalsResponseDto response = vitalsService.createVitals(encounterId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Physiological vitals recorded successfully", response));
    }

    @GetMapping("/encounters/{encounterId}/vitals")
    @Operation(summary = "Get Vitals by Encounter", description = "Retrieves all physiological vitals recorded during a clinical encounter")
    public ResponseEntity<ApiResponse<List<VitalsResponseDto>>> getVitalsByEncounter(
            @Parameter(description = "Encounter UUID") @PathVariable UUID encounterId) {
        List<VitalsResponseDto> response = vitalsService.getVitalsByEncounterId(encounterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
