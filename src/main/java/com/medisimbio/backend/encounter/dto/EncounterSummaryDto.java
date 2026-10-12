package com.medisimbio.backend.encounter.dto;

import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary representation of a clinical encounter for paginated directory and patient list views.
 */
@Schema(description = "Compact summary of a clinical encounter")
public class EncounterSummaryDto {

    @Schema(description = "Encounter UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Human-readable unique encounter reference", example = "ENC-20261008-7K2M")
    private String encounterReference;

    @Schema(description = "Patient UUID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID patientId;

    @Schema(description = "Patient Full Name", example = "Rahul Sharma")
    private String patientName;

    @Schema(description = "Provider UUID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID providerId;

    @Schema(description = "Provider Full Name", example = "Dr. Ananya Sharma")
    private String providerName;

    @Schema(description = "Hospital Facility UUID", example = "123e4567-e89b-12d3-a456-426614174003")
    private UUID hospitalId;

    @Schema(description = "Hospital Facility Name", example = "Apollo Multispeciality Hospital")
    private String hospitalName;

    @Schema(description = "Encounter Type", example = "INITIAL_CONSULTATION")
    private EncounterType encounterType;

    @Schema(description = "Encounter Lifecycle Status", example = "COMPLETED")
    private EncounterStatus status;

    @Schema(description = "Consultation start timestamp")
    private Instant startedAt;

    @Schema(description = "Consultation completion timestamp")
    private Instant endedAt;

    @Schema(description = "Chief presenting complaint", example = "Persistent high fever and headache for 3 days")
    private String chiefComplaint;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    public EncounterSummaryDto() {
    }

    public EncounterSummaryDto(UUID id, String encounterReference, UUID patientId, String patientName,
                               UUID providerId, String providerName, UUID hospitalId, String hospitalName,
                               EncounterType encounterType, EncounterStatus status, Instant startedAt,
                               Instant endedAt, String chiefComplaint, Instant createdAt) {
        this.id = id;
        this.encounterReference = encounterReference;
        this.patientId = patientId;
        this.patientName = patientName;
        this.providerId = providerId;
        this.providerName = providerName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.encounterType = encounterType;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.chiefComplaint = chiefComplaint;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEncounterReference() {
        return encounterReference;
    }

    public void setEncounterReference(String encounterReference) {
        this.encounterReference = encounterReference;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public EncounterStatus getStatus() {
        return status;
    }

    public void setStatus(EncounterStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
