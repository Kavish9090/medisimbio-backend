package com.medisimbio.backend.diagnosis.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a clinical diagnosis recorded during an encounter.
 */
@Entity
@Table(name = "diagnoses")
public class DiagnosisEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false)
    private EncounterEntity encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "diagnosis_code", length = 50)
    private String diagnosisCode;

    @Column(name = "diagnosis_name", length = 255, nullable = false)
    private String diagnosisName;

    @Column(name = "diagnosis_description", length = 1000)
    private String diagnosisDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_type", length = 30, nullable = false)
    private DiagnosisType diagnosisType = DiagnosisType.PRIMARY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private DiagnosisStatus status = DiagnosisStatus.ACTIVE;

    @Column(name = "diagnosed_at", nullable = false)
    private Instant diagnosedAt = Instant.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public DiagnosisEntity() {
    }

    public DiagnosisEntity(UUID id, EncounterEntity encounter, PatientEntity patient, String diagnosisCode,
                           String diagnosisName, String diagnosisDescription, DiagnosisType diagnosisType,
                           DiagnosisStatus status, Instant diagnosedAt, String notes, String recordedBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.encounter = encounter;
        this.patient = patient;
        this.diagnosisCode = diagnosisCode;
        this.diagnosisName = diagnosisName;
        this.diagnosisDescription = diagnosisDescription;
        this.diagnosisType = diagnosisType != null ? diagnosisType : DiagnosisType.PRIMARY;
        this.status = status != null ? status : DiagnosisStatus.ACTIVE;
        this.diagnosedAt = diagnosedAt != null ? diagnosedAt : Instant.now();
        this.notes = notes;
        this.recordedBy = recordedBy;
        this.createdBy = recordedBy;
        this.updatedBy = recordedBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.diagnosedAt == null) {
            this.diagnosedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisName() {
        return diagnosisName;
    }

    public void setDiagnosisName(String diagnosisName) {
        this.diagnosisName = diagnosisName;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
