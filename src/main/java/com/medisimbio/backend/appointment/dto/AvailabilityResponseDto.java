package com.medisimbio.backend.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response DTO for Doctor Availability Schedule.
 */
@Schema(description = "Doctor availability response payload")
public class AvailabilityResponseDto {

    @Schema(description = "Availability UUID")
    private UUID id;

    @Schema(description = "Provider UUID")
    private UUID providerId;

    @Schema(description = "Provider Full Name")
    private String providerName;

    @Schema(description = "Hospital UUID")
    private UUID hospitalId;

    @Schema(description = "Hospital Name")
    private String hospitalName;

    @Schema(description = "Day of week")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Shift start time")
    private LocalTime startTime;

    @Schema(description = "Shift end time")
    private LocalTime endTime;

    @Schema(description = "Slot duration in minutes")
    private Integer slotDurationMinutes;

    @Schema(description = "Active status")
    private boolean active;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Update timestamp")
    private Instant updatedAt;

    public AvailabilityResponseDto() {
    }

    public AvailabilityResponseDto(UUID id, UUID providerId, String providerName,
                                   UUID hospitalId, String hospitalName, DayOfWeek dayOfWeek,
                                   LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes,
                                   boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.providerId = providerId;
        this.providerName = providerName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
