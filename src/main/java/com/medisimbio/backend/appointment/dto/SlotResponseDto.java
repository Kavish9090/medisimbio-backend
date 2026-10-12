package com.medisimbio.backend.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for dynamically calculated consultation time slots.
 */
@Schema(description = "Calculated consultation slot payload")
public class SlotResponseDto {

    @Schema(description = "Consultation date", example = "2026-10-10")
    private LocalDate date;

    @Schema(description = "Slot start time", example = "09:00:00")
    private LocalTime startTime;

    @Schema(description = "Slot end time", example = "09:30:00")
    private LocalTime endTime;

    @Schema(description = "Slot duration in minutes", example = "30")
    private int slotDurationMinutes;

    @Schema(description = "Whether the slot is currently available to be booked", example = "true")
    private boolean available;

    public SlotResponseDto() {
    }

    public SlotResponseDto(LocalDate date, LocalTime startTime, LocalTime endTime,
                           int slotDurationMinutes, boolean available) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.available = available;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
