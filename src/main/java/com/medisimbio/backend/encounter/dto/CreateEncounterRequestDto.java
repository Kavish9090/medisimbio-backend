package com.medisimbio.backend.encounter.dto;

import com.medisimbio.backend.encounter.entity.EncounterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Request payload for creating a clinical encounter from an appointment.
 */
@Schema(description = "Request DTO for creating a clinical encounter")
public class CreateEncounterRequestDto {

    @NotNull(message = "Appointment ID is mandatory")
    @Schema(description = "UUID of the scheduled appointment", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID appointmentId;

    @Schema(description = "Type of clinical interaction", example = "INITIAL_CONSULTATION")
    private EncounterType encounterType = EncounterType.INITIAL_CONSULTATION;

    @Size(max = 500, message = "Chief complaint must not exceed 500 characters")
    @Schema(description = "Patient's primary presenting symptom or complaint", example = "Persistent high fever and headache for 3 days")
    private String chiefComplaint;

    @Schema(description = "Optional custom consultation start time. Defaults to now if omitted.")
    private Instant startedAt;

    public CreateEncounterRequestDto() {
    }

    public CreateEncounterRequestDto(UUID appointmentId, EncounterType encounterType, String chiefComplaint, Instant startedAt) {
        this.appointmentId = appointmentId;
        this.encounterType = encounterType != null ? encounterType : EncounterType.INITIAL_CONSULTATION;
        this.chiefComplaint = chiefComplaint;
        this.startedAt = startedAt;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
}
