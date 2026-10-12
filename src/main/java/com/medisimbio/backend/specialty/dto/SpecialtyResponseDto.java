package com.medisimbio.backend.specialty.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for Medical Specialty.
 */
@Schema(description = "Medical specialty response payload")
public class SpecialtyResponseDto {

    @Schema(description = "Specialty UUID", example = "11111111-1111-1111-1111-111111111002")
    private UUID id;

    @Schema(description = "Specialty Code", example = "CARDIOLOGY")
    private String code;

    @Schema(description = "Display Name", example = "Cardiology")
    private String displayName;

    @Schema(description = "Description", example = "Disorders of the heart and cardiovascular system")
    private String description;

    @Schema(description = "Active status", example = "true")
    private boolean active;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record update timestamp")
    private Instant updatedAt;

    public SpecialtyResponseDto() {
    }

    public SpecialtyResponseDto(UUID id, String code, String displayName, String description,
                                boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
