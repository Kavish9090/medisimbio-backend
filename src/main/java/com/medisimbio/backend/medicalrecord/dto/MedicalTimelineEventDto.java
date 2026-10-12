package com.medisimbio.backend.medicalrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Normalized medical event in a patient's chronological clinical timeline.
 */
@Schema(description = "Normalized clinical timeline event")
public class MedicalTimelineEventDto {

    @Schema(description = "Event category", example = "ENCOUNTER")
    private TimelineEventType eventType;

    @Schema(description = "Unique UUID of the clinical record (Encounter/Diagnosis/Vitals ID)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID eventId;

    @Schema(description = "Timestamp when the clinical event occurred or was measured")
    private Instant occurredAt;

    @Schema(description = "Human-readable event title", example = "Consultation — Dr. Ananya Sharma")
    private String title;

    @Schema(description = "Clinical summary or primary finding", example = "Chief Complaint: High fever. Diagnosis: Acute Bronchitis")
    private String summary;

    @Schema(description = "Associated Encounter UUID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID encounterId;

    @Schema(description = "Associated Encounter Reference", example = "ENC-20261008-7K2M")
    private String encounterReference;

    @Schema(description = "Attending Provider UUID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID providerId;

    @Schema(description = "Attending Provider Full Name", example = "Dr. Ananya Sharma")
    private String providerName;

    @Schema(description = "Hospital Facility UUID", example = "123e4567-e89b-12d3-a456-426614174003")
    private UUID hospitalId;

    @Schema(description = "Hospital Facility Name", example = "Apollo Multispeciality Hospital")
    private String hospitalName;

    @Schema(description = "Status or outcome (e.g. COMPLETED, ACTIVE)", example = "COMPLETED")
    private String status;

    public MedicalTimelineEventDto() {
    }

    public MedicalTimelineEventDto(TimelineEventType eventType, UUID eventId, Instant occurredAt, String title,
                                  String summary, UUID encounterId, String encounterReference, UUID providerId,
                                  String providerName, UUID hospitalId, String hospitalName, String status) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.title = title;
        this.summary = summary;
        this.encounterId = encounterId;
        this.encounterReference = encounterReference;
        this.providerId = providerId;
        this.providerName = providerName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.status = status;
    }

    public TimelineEventType getEventType() {
        return eventType;
    }

    public void setEventType(TimelineEventType eventType) {
        this.eventType = eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
    }

    public String getEncounterReference() {
        return encounterReference;
    }

    public void setEncounterReference(String encounterReference) {
        this.encounterReference = encounterReference;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
