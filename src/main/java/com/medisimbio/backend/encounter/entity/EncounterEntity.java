package com.medisimbio.backend.encounter.entity;

import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a clinical consultation encounter between a patient and a healthcare provider.
 */
@Entity
@Table(name = "encounters")
public class EncounterEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "encounter_reference", length = 30, nullable = false, unique = true)
    private String encounterReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private AppointmentEntity appointment;

    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", length = 30, nullable = false)
    private EncounterType encounterType = EncounterType.INITIAL_CONSULTATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private EncounterStatus status = EncounterStatus.OPEN;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "chief_complaint", length = 500)
    private String chiefComplaint;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(name = "diagnosis_summary", length = 500)
    private String diagnosisSummary;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(name = "follow_up_instructions", length = 500)
    private String followUpInstructions;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public EncounterEntity() {
    }

    public EncounterEntity(UUID id, String encounterReference, PatientEntity patient, ProviderEntity provider,
                           HospitalEntity hospital, AppointmentEntity appointment, EncounterType encounterType,
                           EncounterStatus status, Instant startedAt, String chiefComplaint, String createdBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.encounterReference = encounterReference;
        this.patient = patient;
        this.provider = provider;
        this.hospital = hospital;
        this.appointment = appointment;
        this.encounterType = encounterType != null ? encounterType : EncounterType.INITIAL_CONSULTATION;
        this.status = status != null ? status : EncounterStatus.OPEN;
        this.startedAt = startedAt;
        this.chiefComplaint = chiefComplaint;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
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

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public ProviderEntity getProvider() {
        return provider;
    }

    public void setProvider(ProviderEntity provider) {
        this.provider = provider;
    }

    public HospitalEntity getHospital() {
        return hospital;
    }

    public void setHospital(HospitalEntity hospital) {
        this.hospital = hospital;
    }

    public AppointmentEntity getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentEntity appointment) {
        this.appointment = appointment;
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

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public String getDiagnosisSummary() {
        return diagnosisSummary;
    }

    public void setDiagnosisSummary(String diagnosisSummary) {
        this.diagnosisSummary = diagnosisSummary;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getFollowUpInstructions() {
        return followUpInstructions;
    }

    public void setFollowUpInstructions(String followUpInstructions) {
        this.followUpInstructions = followUpInstructions;
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
