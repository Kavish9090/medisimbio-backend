package com.medisimbio.backend.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request payload for booking a patient consultation appointment.
 */
@Schema(description = "Request payload for booking an appointment")
public class BookAppointmentRequestDto {

    @Schema(description = "Doctor / Provider UUID", example = "33333333-3333-3333-3333-333333333001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Provider ID is required")
    private UUID providerId;

    @Schema(description = "Hospital facility UUID", example = "22222222-2222-2222-2222-222222222001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;

    @Schema(description = "Medical specialty UUID (optional)", example = "11111111-1111-1111-1111-111111111002")
    private UUID specialtyId;

    @Schema(description = "Appointment date (must be today or in the future)", example = "2026-10-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be today or in the future")
    private LocalDate appointmentDate;

    @Schema(description = "Consultation start time (HH:mm)", example = "09:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @Schema(description = "Chief complaint or visit reason summary", example = "Routine cardiac checkup and blood pressure monitoring")
    @Size(max = 500, message = "Visit reason cannot exceed 500 characters")
    private String visitReason;

    public BookAppointmentRequestDto() {
    }

    public BookAppointmentRequestDto(UUID providerId, UUID hospitalId, UUID specialtyId,
                                     LocalDate appointmentDate, LocalTime startTime, String visitReason) {
        this.providerId = providerId;
        this.hospitalId = hospitalId;
        this.specialtyId = specialtyId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.visitReason = visitReason;
    }

    public BookAppointmentRequestDto(UUID providerId, UUID hospitalId,
                                     LocalDate appointmentDate, LocalTime startTime, String visitReason) {
        this(providerId, hospitalId, null, appointmentDate, startTime, visitReason);
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public UUID getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(UUID specialtyId) {
        this.specialtyId = specialtyId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public void setVisitReason(String visitReason) {
        this.visitReason = visitReason;
    }
}
