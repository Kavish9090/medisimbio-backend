package com.medisimbio.backend.appointment.controller;

import com.medisimbio.backend.appointment.dto.AvailabilityResponseDto;
import com.medisimbio.backend.appointment.dto.CreateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.dto.UpdateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.service.AvailabilityService;
import com.medisimbio.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Doctor Availability schedules.
 */
@RestController
@RequestMapping("/api/v1/providers/{providerId}/availability")
@Tag(name = "Doctor Availability", description = "Endpoints for managing doctor recurring shifts and hospital availability schedules")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @Operation(summary = "Create availability schedule", description = "Defines a doctor's weekly shift window at a specific hospital")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> createAvailability(
            @PathVariable UUID providerId,
            @Valid @RequestBody CreateAvailabilityRequestDto requestDto) {
        AvailabilityResponseDto response = availabilityService.createAvailability(providerId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability schedule created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List doctor availability schedules", description = "Retrieves doctor schedules filtered optionally by hospital, day of week, or active status")
    public ResponseEntity<ApiResponse<List<AvailabilityResponseDto>>> getAvailabilities(
            @PathVariable UUID providerId,
            @RequestParam(required = false) UUID hospitalId,
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(required = false) Boolean active) {
        List<AvailabilityResponseDto> response = availabilityService.getAvailabilities(providerId, hospitalId, dayOfWeek, active);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get availability by ID", description = "Retrieves a specific doctor availability schedule")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> getAvailabilityById(
            @PathVariable UUID providerId,
            @PathVariable UUID id) {
        AvailabilityResponseDto response = availabilityService.getAvailabilityById(providerId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Update availability schedule", description = "Updates a doctor's availability schedule")
    public ResponseEntity<ApiResponse<AvailabilityResponseDto>> updateAvailability(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAvailabilityRequestDto requestDto) {
        AvailabilityResponseDto response = availabilityService.updateAvailability(providerId, id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Availability schedule updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete availability schedule", description = "Deletes an availability schedule")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @PathVariable UUID providerId,
            @PathVariable UUID id) {
        availabilityService.deleteAvailability(providerId, id);
        return ResponseEntity.ok(ApiResponse.success("Availability schedule deleted successfully", null));
    }
}
