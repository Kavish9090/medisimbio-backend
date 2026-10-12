package com.medisimbio.backend.hospital.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entity representing hospital staff and clinical membership authorization.
 */
@Entity
@Table(name = "hospital_staff", uniqueConstraints = {
        @UniqueConstraint(name = "uq_hospital_staff", columnNames = {"hospital_id", "auth_user_id"})
})
public class HospitalStaffEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @Column(name = "auth_user_id", length = 128, nullable = false)
    private String authUserId;

    @Column(name = "staff_name", length = 150)
    private String staffName;

    @Column(name = "role", length = 50, nullable = false)
    private String role = "STAFF";

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public HospitalStaffEntity() {
    }

    public HospitalStaffEntity(UUID id, HospitalEntity hospital, String authUserId, String staffName, String role, boolean active) {
        this.id = id != null ? id : UUID.randomUUID();
        this.hospital = hospital;
        this.authUserId = authUserId;
        this.staffName = staffName;
        this.role = role != null ? role : "STAFF";
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

    public HospitalEntity getHospital() {
        return hospital;
    }

    public void setHospital(HospitalEntity hospital) {
        this.hospital = hospital;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
