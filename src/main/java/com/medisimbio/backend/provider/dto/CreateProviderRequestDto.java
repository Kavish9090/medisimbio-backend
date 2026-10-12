package com.medisimbio.backend.provider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Set;
import java.util.UUID;

/**
 * Request payload for creating/registering a healthcare provider (doctor).
 */
@Schema(description = "Request payload for registering a healthcare provider")
public class CreateProviderRequestDto {

    @Schema(description = "Doctor's full name with title", example = "Dr. Rajesh Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @Schema(description = "Medical council registration number", example = "MCI-2015-89412", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Registration number is required")
    @Size(max = 100, message = "Registration number cannot exceed 100 characters")
    private String professionalRegistrationNumber;

    @Schema(description = "Qualifications & degrees", example = "MBBS, MD - General Medicine, DM - Cardiology", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Qualification is required")
    @Size(max = 200, message = "Qualification cannot exceed 200 characters")
    private String qualification;

    @Schema(description = "Years of clinical experience", example = "12")
    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears = 0;

    @Schema(description = "Profile avatar image URL", example = "https://example.com/photos/dr-rajesh.jpg")
    @Size(max = 500, message = "Profile photo URL cannot exceed 500 characters")
    private String profilePhotoUrl;

    @Schema(description = "Official/contact email", example = "dr.rajesh.sharma@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Official/contact mobile phone", example = "+919811223344", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Mobile number is required")
    @Size(max = 20, message = "Mobile number cannot exceed 20 characters")
    private String mobileNumber;

    @Schema(description = "Set of medical specialty UUIDs to associate upon creation")
    private Set<UUID> specialtyIds;

    @Schema(description = "Authenticated user account ID linked to this doctor", example = "auth_doctor_1")
    @Size(max = 128, message = "Auth user ID cannot exceed 128 characters")
    private String authUserId;

    public CreateProviderRequestDto() {
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfessionalRegistrationNumber() {
        return professionalRegistrationNumber;
    }

    public void setProfessionalRegistrationNumber(String professionalRegistrationNumber) {
        this.professionalRegistrationNumber = professionalRegistrationNumber;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Set<UUID> getSpecialtyIds() {
        return specialtyIds;
    }

    public void setSpecialtyIds(Set<UUID> specialtyIds) {
        this.specialtyIds = specialtyIds;
    }
}
