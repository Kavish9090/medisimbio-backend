package com.medisimbio.backend.medid.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Authoritative Med-ID Entity.
 */
@Entity
@Table(name = "med_ids")
public class MedIdEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private PatientEntity patient;

    @Column(name = "med_id_number", length = 20, nullable = false, unique = true)
    private String medIdNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MedIdStatus status = MedIdStatus.ACTIVE;

    @Column(name = "qr_token", length = 255, nullable = false)
    private String qrToken;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    public MedIdEntity() {
    }

    public MedIdEntity(UUID id, PatientEntity patient, String medIdNumber, String qrToken) {
        this.id = id != null ? id : UUID.randomUUID();
        this.patient = patient;
        this.medIdNumber = medIdNumber;
        this.qrToken = qrToken;
        this.status = MedIdStatus.ACTIVE;
        this.issuedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.issuedAt == null) {
            this.issuedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public String getMedIdNumber() {
        return medIdNumber;
    }

    public void setMedIdNumber(String medIdNumber) {
        this.medIdNumber = medIdNumber;
    }

    public MedIdStatus getStatus() {
        return status;
    }

    public void setStatus(MedIdStatus status) {
        this.status = status;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
