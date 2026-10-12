package com.medisimbio.backend.diagnosis.dto;

import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import com.medisimbio.backend.diagnosis.entity.DiagnosisType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed representation of a clinical diagnosis.
 */
@Schema(description = "Clinical diagnosis response")
public class DiagnosisResponseDto {

    @Schema(description = "Diagnosis UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Encounter UUID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID encounterId;

    @Schema(description = "Encounter Reference", example = "ENC-20261008-7K2M")
    private String encounterReference;

    @Schema(description = "Patient UUID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID patientId;

    @Schema(description = "Patient Full Name", example = "Rahul Sharma")
    private String patientName;

    @Schema(description = "Diagnosis Title / Name", example = "Acute Bronchitis")
    private String diagnosisName;

    @Schema(description = "Standard Code (e.g. ICD-10)", example = "J20.9")
    private String diagnosisCode;

    @Schema(description = "Diagnosis Description", example = "Acute inflammation of tracheobronchial tree")
    private String diagnosisDescription;

    @Schema(description = "Diagnosis Classification Type", example = "PRIMARY")
    private DiagnosisType diagnosisType;

    @Schema(description = "Diagnosis Clinical Status", example = "ACTIVE")
    private DiagnosisStatus status;

    @Schema(description = "Diagnosed timestamp")
    private Instant diagnosedAt;

    @Schema(description = "Clinical notes and provider observations", example = "Crackles heard on auscultation.")
    private String notes;

    @Schema(description = "Provider or user who recorded the diagnosis", example = "auth_doctor_1")
    private String recordedBy;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record last modified timestamp")
    private Instant updatedAt;

    public DiagnosisResponseDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getDiagnosisName() {
        return diagnosisName;
    }

    public void setDiagnosisName(String diagnosisName) {
        this.diagnosisName = diagnosisName;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisDescription() {
        return diagnosisDescription;
    }

    public void setDiagnosisDescription(String diagnosisDescription) {
        this.diagnosisDescription = diagnosisDescription;
    }

    public DiagnosisType getDiagnosisType() {
        return diagnosisType;
    }

    public void setDiagnosisType(DiagnosisType diagnosisType) {
        this.diagnosisType = diagnosisType;
    }

    public DiagnosisStatus getStatus() {
        return status;
    }

    public void setStatus(DiagnosisStatus status) {
        this.status = status;
    }

    public Instant getDiagnosedAt() {
        return diagnosedAt;
    }

    public void setDiagnosedAt(Instant diagnosedAt) {
        this.diagnosedAt = diagnosedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
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
