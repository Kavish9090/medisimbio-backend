package com.medisimbio.backend.appointment.controller;

import com.medisimbio.backend.appointment.dto.*;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.service.AppointmentService;
import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller for Appointment booking, lifecycle management, and directory views.
 */
@RestController
@Tag(name = "Appointments", description = "Endpoints for consultation booking, cancellation, rescheduling, and directory views")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/api/v1/appointments")
    @Operation(summary = "Book consultation appointment", description = "Books a consultation appointment for the currently authenticated patient")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> bookAppointment(
            @Valid @RequestBody BookAppointmentRequestDto requestDto) {
        AppointmentResponseDto response = appointmentService.bookAppointment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", response));
    }

    @GetMapping("/api/v1/appointments/me")
    @Operation(summary = "Get my appointments", description = "Retrieves a paginated list of appointments for the authenticated patient")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentSummaryDto>>> getMyAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20, sort = "appointmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<AppointmentSummaryDto> response = appointmentService.getPatientAppointments(status, date, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"/api/v1/appointments/me/{id}", "/api/v1/appointments/{id}"})
    @Operation(summary = "Get appointment details", description = "Retrieves full details of a specific appointment belonging to the authenticated patient")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> getMyAppointmentById(@PathVariable UUID id) {
        AppointmentResponseDto response = appointmentService.getPatientAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/appointments/{id}/cancel")
    @Operation(summary = "Cancel appointment", description = "Cancels a scheduled or confirmed appointment")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> cancelAppointment(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelAppointmentRequestDto requestDto) {
        AppointmentResponseDto response = appointmentService.cancelAppointment(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", response));
    }

    @PostMapping("/api/v1/appointments/{id}/reschedule")
    @Operation(summary = "Reschedule appointment", description = "Reschedules an existing appointment to a new date and time slot")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequestDto requestDto) {
        AppointmentResponseDto response = appointmentService.rescheduleAppointment(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled successfully", response));
    }

    @GetMapping("/api/v1/providers/{providerId}/appointments")
    @Operation(summary = "Doctor appointment directory", description = "Retrieves appointments for a specific doctor filtered by date, status, or hospital")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentSummaryDto>>> getProviderAppointments(
            @PathVariable UUID providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID hospitalId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<AppointmentSummaryDto> response = appointmentService.getProviderAppointments(providerId, date, status, hospitalId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/hospitals/{hospitalId}/appointments")
    @Operation(summary = "Hospital appointment directory", description = "Retrieves appointments for a specific hospital facility filtered by date, doctor, or status")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentSummaryDto>>> getHospitalAppointments(
            @PathVariable UUID hospitalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID providerId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<AppointmentSummaryDto> response = appointmentService.getHospitalAppointments(hospitalId, date, providerId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
