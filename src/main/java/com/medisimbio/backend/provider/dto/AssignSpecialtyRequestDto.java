package com.medisimbio.backend.provider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

/**
 * Request payload for associating specialties with a healthcare provider.
 */
@Schema(description = "Request payload for assigning specialties to a provider")
public class AssignSpecialtyRequestDto {

    @Schema(description = "Set of Specialty UUIDs to assign", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one specialty UUID is required")
    private Set<UUID> specialtyIds;

    public AssignSpecialtyRequestDto() {
    }

    public AssignSpecialtyRequestDto(Set<UUID> specialtyIds) {
        this.specialtyIds = specialtyIds;
    }

    public Set<UUID> getSpecialtyIds() {
        return specialtyIds;
    }

    public void setSpecialtyIds(Set<UUID> specialtyIds) {
        this.specialtyIds = specialtyIds;
    }
}
