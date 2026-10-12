package com.medisimbio.backend.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request payload for rescheduling an existing appointment to a new date and time slot.
 */
@Schema(description = "Request payload for rescheduling an appointment")
public class RescheduleAppointmentRequestDto {

    @Schema(description = "New appointment date", example = "2026-10-18", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "New appointment date is required")
    @FutureOrPresent(message = "New appointment date must be today or in the future")
    private LocalDate newDate;

    @Schema(description = "New consultation start time (HH:mm)", example = "10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "New start time is required")
    private LocalTime newStartTime;

    @Schema(description = "New hospital facility UUID (optional if changing hospital)", example = "22222222-2222-2222-2222-222222222001")
    private UUID newHospitalId;

    public RescheduleAppointmentRequestDto() {
    }

    public RescheduleAppointmentRequestDto(LocalDate newDate, LocalTime newStartTime) {
        this(newDate, newStartTime, null);
    }

    public RescheduleAppointmentRequestDto(LocalDate newDate, LocalTime newStartTime, UUID newHospitalId) {
        this.newDate = newDate;
        this.newStartTime = newStartTime;
        this.newHospitalId = newHospitalId;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }

    public LocalTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public UUID getNewHospitalId() {
        return newHospitalId;
    }

    public void setNewHospitalId(UUID newHospitalId) {
        this.newHospitalId = newHospitalId;
    }
}
