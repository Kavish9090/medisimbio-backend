package com.medisimbio.backend.specialty.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entity representing a Medical Specialty (e.g., Cardiology, Dermatology).
 */
@Entity
@Table(name = "specialties")
public class SpecialtyEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public SpecialtyEntity() {
    }

    public SpecialtyEntity(UUID id, String code, String displayName, String description, boolean active) {
        this.id = id != null ? id : UUID.randomUUID();
        this.code = code;
        this.displayName = displayName;
        this.description = description;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
