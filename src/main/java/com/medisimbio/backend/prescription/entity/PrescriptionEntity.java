package com.medisimbio.backend.prescription.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.common.exception.ConflictException;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an electronic prescription issued during a clinical consultation.
 */
@Entity
@Table(name = "prescriptions")
public class PrescriptionEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "prescription_reference", length = 40, nullable = false, unique = true)
    private String prescriptionReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false)
    private EncounterEntity encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.DRAFT;

    @Column(name = "prescribed_at", nullable = false)
    private Instant prescribedAt = Instant.now();

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "clinical_instructions", columnDefinition = "TEXT")
    private String clinicalInstructions;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaces_prescription_id")
    private PrescriptionEntity replacesPrescription;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_by", length = 128)
    private String cancelledBy;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "updated_by", length = 128)
    private String updatedBy;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    private List<PrescriptionItemEntity> items = new ArrayList<>();

    public PrescriptionEntity() {
    }

    public PrescriptionEntity(UUID id, String prescriptionReference, PatientEntity patient, EncounterEntity encounter,
                              ProviderEntity provider, HospitalEntity hospital, PrescriptionStatus status,
                              Instant prescribedAt, LocalDate validUntil, String clinicalInstructions,
                              Integer versionNumber, PrescriptionEntity replacesPrescription, String createdBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.prescriptionReference = prescriptionReference;
        this.patient = patient;
        this.encounter = encounter;
        this.provider = provider;
        this.hospital = hospital;
        this.status = status != null ? status : PrescriptionStatus.DRAFT;
        this.prescribedAt = prescribedAt != null ? prescribedAt : Instant.now();
        this.validUntil = validUntil;
        this.clinicalInstructions = clinicalInstructions;
        this.versionNumber = versionNumber != null ? versionNumber : 1;
        this.replacesPrescription = replacesPrescription;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.prescribedAt == null) {
            this.prescribedAt = Instant.now();
        }
        if (this.versionNumber == null) {
            this.versionNumber = 1;
        }
    }

    public void addItem(PrescriptionItemEntity item) {
        if (this.status != PrescriptionStatus.DRAFT) {
            throw new ConflictException("Prescription items can only be modified when prescription is in DRAFT status");
        }
        items.add(item);
        item.setPrescription(this);
    }

    public void removeItem(PrescriptionItemEntity item) {
        if (this.status != PrescriptionStatus.DRAFT) {
            throw new ConflictException("Prescription items can only be modified when prescription is in DRAFT status");
        }
        items.remove(item);
        item.setPrescription(null);
    }

    public void clearItems() {
        if (this.status != PrescriptionStatus.DRAFT) {
            throw new ConflictException("Prescription items can only be modified when prescription is in DRAFT status");
        }
        items.forEach(item -> item.setPrescription(null));
        items.clear();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPrescriptionReference() {
        return prescriptionReference;
    }

    public void setPrescriptionReference(String prescriptionReference) {
        this.prescriptionReference = prescriptionReference;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
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

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public Instant getPrescribedAt() {
        return prescribedAt;
    }

    public void setPrescribedAt(Instant prescribedAt) {
        this.prescribedAt = prescribedAt;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public String getClinicalInstructions() {
        return clinicalInstructions;
    }

    public void setClinicalInstructions(String clinicalInstructions) {
        this.clinicalInstructions = clinicalInstructions;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public PrescriptionEntity getReplacesPrescription() {
        return replacesPrescription;
    }

    public void setReplacesPrescription(PrescriptionEntity replacesPrescription) {
        this.replacesPrescription = replacesPrescription;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
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

    public List<PrescriptionItemEntity> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemEntity> items) {
        this.items = items;
    }
}
