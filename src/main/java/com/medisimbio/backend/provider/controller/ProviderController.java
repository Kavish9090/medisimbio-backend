package com.medisimbio.backend.provider.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.provider.dto.*;
import com.medisimbio.backend.provider.service.ProviderService;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Healthcare Provider (Doctor) Management.
 */
@RestController
@RequestMapping("/api/v1/providers")
@Tag(name = "Healthcare Provider Catalog", description = "Endpoints for doctor discovery, registration, and clinical affiliations")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    @Operation(summary = "Register healthcare provider", description = "Creates a new doctor profile in the catalog")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> createProvider(
            @Valid @RequestBody CreateProviderRequestDto requestDto) {
        ProviderResponseDto response = providerService.createProvider(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Healthcare provider registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get provider by ID", description = "Retrieves complete profile, specialties, and affiliations of a doctor")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> getProviderById(@PathVariable UUID id) {
        ProviderResponseDto response = providerService.getProviderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update provider profile", description = "Partially updates details of an existing doctor")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> updateProvider(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProviderRequestDto requestDto) {
        ProviderResponseDto response = providerService.updateProvider(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Healthcare provider updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search and list providers", description = "Retrieves a paginated list of providers with optional filters for specialty, hospital, city, active status, or keyword search")
    public ResponseEntity<ApiResponse<PageResponse<ProviderSummaryDto>>> getProviders(
            @RequestParam(required = false) UUID specialtyId,
            @RequestParam(required = false) UUID hospitalId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<ProviderSummaryDto> response = providerService.getProviders(specialtyId, hospitalId, city, active, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/specialties")
    @Operation(summary = "Assign specialties to provider", description = "Assigns one or more medical specialties to a doctor")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> assignSpecialties(
            @PathVariable UUID id,
            @Valid @RequestBody AssignSpecialtyRequestDto requestDto) {
        ProviderResponseDto response = providerService.assignSpecialties(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Specialties assigned successfully", response));
    }

    @DeleteMapping("/{id}/specialties/{specialtyId}")
    @Operation(summary = "Remove specialty from provider", description = "Disassociates a specialty from a doctor")
    public ResponseEntity<ApiResponse<Void>> removeSpecialty(
            @PathVariable UUID id,
            @PathVariable UUID specialtyId) {
        providerService.removeSpecialty(id, specialtyId);
        return ResponseEntity.ok(ApiResponse.success("Specialty removed successfully", null));
    }

    @GetMapping("/{id}/specialties")
    @Operation(summary = "Get provider specialties", description = "Lists all medical specialties associated with a doctor")
    public ResponseEntity<ApiResponse<List<SpecialtyResponseDto>>> getSpecialtiesForProvider(
            @PathVariable UUID id) {
        List<SpecialtyResponseDto> response = providerService.getSpecialtiesForProvider(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/hospitals")
    @Operation(summary = "Affiliate provider with hospital", description = "Creates or updates an affiliation between a doctor and a hospital")
    public ResponseEntity<ApiResponse<ProviderHospitalResponseDto>> assignHospital(
            @PathVariable UUID id,
            @Valid @RequestBody AssignHospitalRequestDto requestDto) {
        ProviderHospitalResponseDto response = providerService.assignHospital(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Hospital affiliation updated successfully", response));
    }

    @DeleteMapping("/{id}/hospitals/{hospitalId}")
    @Operation(summary = "Remove hospital affiliation", description = "Removes an affiliation between a doctor and a hospital")
    public ResponseEntity<ApiResponse<Void>> removeHospital(
            @PathVariable UUID id,
            @PathVariable UUID hospitalId) {
        providerService.removeHospital(id, hospitalId);
        return ResponseEntity.ok(ApiResponse.success("Hospital affiliation removed successfully", null));
    }

    @GetMapping("/{id}/hospitals")
    @Operation(summary = "Get provider hospital affiliations", description = "Lists all hospital affiliations for a doctor")
    public ResponseEntity<ApiResponse<List<ProviderHospitalResponseDto>>> getHospitalsForProvider(
            @PathVariable UUID id) {
        List<ProviderHospitalResponseDto> response = providerService.getHospitalsForProvider(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
