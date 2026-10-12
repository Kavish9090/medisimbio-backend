package com.medisimbio.backend.medicalrecord.dto;

/**
 * Spring Data Projection interface for unified SQL query timeline events.
 * Uses Object return types for IDs and timestamps to ensure cross-database JDBC compatibility (PostgreSQL & H2).
 */
public interface MedicalTimelineEventProjection {

    String getEventType();

    Object getEventId();

    Object getOccurredAt();

    String getTitle();

    String getSummary();

    Object getEncounterId();

    String getEncounterReference();

    Object getProviderId();

    String getProviderName();

    Object getHospitalId();

    String getHospitalName();

    String getStatus();
}
