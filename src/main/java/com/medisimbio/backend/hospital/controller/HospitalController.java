package com.medisimbio.backend.hospital.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.hospital.dto.CreateHospitalRequestDto;
import com.medisimbio.backend.hospital.dto.HospitalResponseDto;
import com.medisimbio.backend.hospital.dto.HospitalSummaryDto;
import com.medisimbio.backend.hospital.dto.UpdateHospitalRequestDto;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.service.HospitalService;
import com.medisimbio.backend.provider.dto.ProviderSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Hospital and Medical Facility Catalog.
 */
@RestController
@RequestMapping("/api/v1/hospitals")
@Tag(name = "Hospital Catalog", description = "Endpoints for discovering, searching, and managing hospitals and clinics")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    @Operation(summary = "Register hospital", description = "Creates a new hospital or healthcare facility entry")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> createHospital(
            @Valid @RequestBody CreateHospitalRequestDto requestDto) {
        HospitalResponseDto response = hospitalService.createHospital(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hospital registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hospital by ID", description = "Retrieves full details of a specific hospital")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> getHospitalById(@PathVariable UUID id) {
        HospitalResponseDto response = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update hospital", description = "Partially updates an existing hospital's details")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> updateHospital(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHospitalRequestDto requestDto) {
        HospitalResponseDto response = hospitalService.updateHospital(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Hospital updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search and list hospitals", description = "Retrieves a paginated list of hospitals with optional filters for type, city, state, active status, or keyword search")
    public ResponseEntity<ApiResponse<PageResponse<HospitalSummaryDto>>> getHospitals(
            @RequestParam(required = false) HospitalType type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<HospitalSummaryDto> response = hospitalService.getHospitals(type, city, state, active, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/providers")
    @Operation(summary = "List doctors affiliated with hospital", description = "Retrieves a paginated list of doctors and practitioners affiliated with this hospital")
    public ResponseEntity<ApiResponse<PageResponse<ProviderSummaryDto>>> getProvidersForHospital(
            @PathVariable UUID id,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ProviderSummaryDto> response = hospitalService.getProvidersForHospital(id, active, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
