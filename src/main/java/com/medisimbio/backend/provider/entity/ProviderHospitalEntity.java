package com.medisimbio.backend.provider.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing an affiliation relationship between a Provider (Doctor) and a Hospital.
 */
@Entity
@Table(name = "provider_hospitals", uniqueConstraints = {
        @UniqueConstraint(name = "uq_provider_hospital", columnNames = {"provider_id", "hospital_id"})
})
public class ProviderHospitalEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ProviderHospitalEntity() {
    }

    public ProviderHospitalEntity(UUID id, ProviderEntity provider, HospitalEntity hospital,
                                  String department, BigDecimal consultationFee, boolean active) {
        this.id = id != null ? id : UUID.randomUUID();
        this.provider = provider;
        this.hospital = hospital;
        this.department = department;
        this.consultationFee = consultationFee;
        this.active = active;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
