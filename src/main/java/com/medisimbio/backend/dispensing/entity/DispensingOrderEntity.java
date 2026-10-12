package com.medisimbio.backend.dispensing.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a medicine dispensing order derived from a finalized clinical prescription.
 */
@Entity
@Table(name = "dispensing_orders")
public class DispensingOrderEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dispensing_reference", length = 40, nullable = false, unique = true)
    private String dispensingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PrescriptionEntity prescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private DispensingOrderStatus status = DispensingOrderStatus.PENDING;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt = Instant.now();

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "updated_by", length = 128)
    private String updatedBy;

    @OneToMany(mappedBy = "dispensingOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DispensingOrderItemEntity> items = new ArrayList<>();

    public DispensingOrderEntity() {
    }

    public DispensingOrderEntity(UUID id, String dispensingReference, PrescriptionEntity prescription,
                                 PatientEntity patient, HospitalEntity hospital, DispensingOrderStatus status,
                                 Instant orderedAt, String notes, String createdBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.dispensingReference = dispensingReference;
        this.prescription = prescription;
        this.patient = patient;
        this.hospital = hospital;
        this.status = status != null ? status : DispensingOrderStatus.PENDING;
        this.orderedAt = orderedAt != null ? orderedAt : Instant.now();
        this.notes = notes;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.orderedAt == null) {
            this.orderedAt = Instant.now();
        }
    }

    public void addItem(DispensingOrderItemEntity item) {
        items.add(item);
        item.setDispensingOrder(this);
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDispensingReference() {
        return dispensingReference;
    }

    public void setDispensingReference(String dispensingReference) {
        this.dispensingReference = dispensingReference;
    }

    public PrescriptionEntity getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionEntity prescription) {
        this.prescription = prescription;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public HospitalEntity getHospital() {
        return hospital;
    }

    public void setHospital(HospitalEntity hospital) {
        this.hospital = hospital;
    }

    public DispensingOrderStatus getStatus() {
        return status;
    }

    public void setStatus(DispensingOrderStatus status) {
        this.status = status;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public List<DispensingOrderItemEntity> getItems() {
        return items;
    }

    public void setItems(List<DispensingOrderItemEntity> items) {
        this.items = items;
    }
}
