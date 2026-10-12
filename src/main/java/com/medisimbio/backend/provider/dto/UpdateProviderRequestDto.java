package com.medisimbio.backend.provider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating healthcare provider details.
 */
@Schema(description = "Request payload for updating healthcare provider details")
public class UpdateProviderRequestDto {

    @Schema(description = "Doctor's full name with title", example = "Dr. Rajesh Sharma")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @Schema(description = "Qualifications & degrees", example = "MBBS, MD - General Medicine, DM - Cardiology")
    @Size(max = 200, message = "Qualification cannot exceed 200 characters")
    private String qualification;

    @Schema(description = "Years of clinical experience", example = "14")
    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @Schema(description = "Profile avatar image URL", example = "https://example.com/photos/dr-rajesh.jpg")
    @Size(max = 500, message = "Profile photo URL cannot exceed 500 characters")
    private String profilePhotoUrl;

    @Schema(description = "Official/contact email", example = "dr.rajesh.sharma@example.com")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Official/contact mobile phone", example = "+919811223344")
    @Size(max = 20, message = "Mobile number cannot exceed 20 characters")
    private String mobileNumber;

    @Schema(description = "Verification status", example = "VERIFIED")
    @Size(max = 50, message = "Verification status cannot exceed 50 characters")
    private String verificationStatus;

    @Schema(description = "Active status", example = "true")
    private Boolean active;

    @Schema(description = "Authenticated user account ID linked to this doctor", example = "auth_doctor_1")
    @Size(max = 128, message = "Auth user ID cannot exceed 128 characters")
    private String authUserId;

    public UpdateProviderRequestDto() {
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

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
