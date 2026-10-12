package com.medisimbio.backend.specialty.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.specialty.dto.CreateSpecialtyRequestDto;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import com.medisimbio.backend.specialty.service.SpecialtyService;
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
 * REST Controller for Medical Specialties Catalog.
 */
@RestController
@RequestMapping("/api/v1/specialties")
@Tag(name = "Specialty Catalog", description = "Endpoints for querying and managing medical specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping
    @Operation(summary = "Create a medical specialty", description = "Registers a new medical specialty in the catalog")
    public ResponseEntity<ApiResponse<SpecialtyResponseDto>> createSpecialty(
            @Valid @RequestBody CreateSpecialtyRequestDto requestDto) {
        SpecialtyResponseDto response = specialtyService.createSpecialty(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medical specialty created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specialty by ID", description = "Retrieves details of a specific medical specialty")
    public ResponseEntity<ApiResponse<SpecialtyResponseDto>> getSpecialtyById(@PathVariable UUID id) {
        SpecialtyResponseDto response = specialtyService.getSpecialtyById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get specialty by Code", description = "Retrieves details of a specialty by its unique uppercase code")
    public ResponseEntity<ApiResponse<SpecialtyResponseDto>> getSpecialtyByCode(@PathVariable String code) {
        SpecialtyResponseDto response = specialtyService.getSpecialtyByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List medical specialties", description = "Retrieves a paginated list of medical specialties with optional active filter")
    public ResponseEntity<ApiResponse<PageResponse<SpecialtyResponseDto>>> getSpecialties(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "displayName", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<SpecialtyResponseDto> response = specialtyService.getSpecialties(active, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
