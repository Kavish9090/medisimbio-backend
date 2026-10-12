package com.medisimbio.backend.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request payload for defining a doctor's recurring availability schedule.
 */
@Schema(description = "Request payload for creating doctor availability schedule")
public class CreateAvailabilityRequestDto {

    @Schema(description = "Hospital UUID", example = "22222222-2222-2222-2222-222222222001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;

    @Schema(description = "Day of week", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Shift start time (HH:mm)", example = "09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @Schema(description = "Shift end time (HH:mm)", example = "13:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Schema(description = "Duration per slot in minutes", example = "30")
    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    @Max(value = 180, message = "Slot duration cannot exceed 180 minutes")
    private Integer slotDurationMinutes = 30;

    public CreateAvailabilityRequestDto() {
    }

    public CreateAvailabilityRequestDto(UUID hospitalId, DayOfWeek dayOfWeek, LocalTime startTime,
                                        LocalTime endTime, Integer slotDurationMinutes) {
        this.hospitalId = hospitalId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes != null ? slotDurationMinutes : 30;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }
}
