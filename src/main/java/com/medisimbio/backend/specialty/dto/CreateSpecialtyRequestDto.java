package com.medisimbio.backend.specialty.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new medical specialty.
 */
@Schema(description = "Request payload for creating a medical specialty")
public class CreateSpecialtyRequestDto {

    @Schema(description = "Unique code for specialty", example = "CARDIOLOGY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Specialty code is required")
    @Size(max = 50, message = "Specialty code cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Specialty code must contain only uppercase letters, numbers, and underscores")
    private String code;

    @Schema(description = "Human-readable display name", example = "Cardiology", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Specialty display name is required")
    @Size(max = 100, message = "Specialty display name cannot exceed 100 characters")
    private String displayName;

    @Schema(description = "Description of specialty", example = "Disorders of the heart and cardiovascular system")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public CreateSpecialtyRequestDto() {
    }

    public CreateSpecialtyRequestDto(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
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
}
