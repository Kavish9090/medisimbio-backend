package com.medisimbio.backend.provider.dto;

import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Detailed Response DTO for Healthcare Provider.
 */
@Schema(description = "Healthcare Provider detailed response payload")
public class ProviderResponseDto {

    @Schema(description = "Provider UUID", example = "33333333-3333-3333-3333-333333333001")
    private UUID id;

    @Schema(description = "Full Name", example = "Dr. Rajesh Sharma")
    private String fullName;

    @Schema(description = "Medical council registration number", example = "MCI-2015-89412")
    private String professionalRegistrationNumber;

    @Schema(description = "Qualification", example = "MBBS, MD - General Medicine, DM - Cardiology")
    private String qualification;

    @Schema(description = "Experience Years", example = "12")
    private Integer experienceYears;

    @Schema(description = "Profile Photo URL", example = "https://example.com/photos/dr-rajesh.jpg")
    private String profilePhotoUrl;

    @Schema(description = "Email", example = "dr.rajesh.sharma@example.com")
    private String email;

    @Schema(description = "Mobile Number", example = "+919811223344")
    private String mobileNumber;

    @Schema(description = "Verification Status", example = "VERIFIED")
    private String verificationStatus;

    @Schema(description = "Active Status", example = "true")
    private boolean active;

    @Schema(description = "Associated Medical Specialties")
    private List<SpecialtyResponseDto> specialties = new ArrayList<>();

    @Schema(description = "Hospital Affiliations")
    private List<ProviderHospitalResponseDto> hospitalAffiliations = new ArrayList<>();

    @Schema(description = "Authenticated user account ID linked to this doctor", example = "auth_doctor_1")
    private String authUserId;

    @Schema(description = "Creation Timestamp")
    private Instant createdAt;

    @Schema(description = "Update Timestamp")
    private Instant updatedAt;

    public ProviderResponseDto() {
    }

    public ProviderResponseDto(UUID id, String fullName, String professionalRegistrationNumber,
                               String qualification, Integer experienceYears, String profilePhotoUrl,
                               String email, String mobileNumber, String verificationStatus,
                               boolean active, List<SpecialtyResponseDto> specialties,
                               List<ProviderHospitalResponseDto> hospitalAffiliations,
                               String authUserId,
                               Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.professionalRegistrationNumber = professionalRegistrationNumber;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.profilePhotoUrl = profilePhotoUrl;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.verificationStatus = verificationStatus;
        this.active = active;
        this.specialties = specialties != null ? specialties : new ArrayList<>();
        this.hospitalAffiliations = hospitalAffiliations != null ? hospitalAffiliations : new ArrayList<>();
        this.authUserId = authUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ProviderResponseDto(UUID id, String fullName, String professionalRegistrationNumber,
                               String qualification, Integer experienceYears, String profilePhotoUrl,
                               String email, String mobileNumber, String verificationStatus,
                               boolean active, List<SpecialtyResponseDto> specialties,
                               List<ProviderHospitalResponseDto> hospitalAffiliations,
                               Instant createdAt, Instant updatedAt) {
        this(id, fullName, professionalRegistrationNumber, qualification, experienceYears,
                profilePhotoUrl, email, mobileNumber, verificationStatus, active,
                specialties, hospitalAffiliations, null, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SpecialtyResponseDto> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<SpecialtyResponseDto> specialties) {
        this.specialties = specialties;
    }

    public List<ProviderHospitalResponseDto> getHospitalAffiliations() {
        return hospitalAffiliations;
    }

    public void setHospitalAffiliations(List<ProviderHospitalResponseDto> hospitalAffiliations) {
        this.hospitalAffiliations = hospitalAffiliations;
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

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }
}
