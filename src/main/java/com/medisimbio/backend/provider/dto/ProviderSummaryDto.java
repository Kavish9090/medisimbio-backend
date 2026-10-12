package com.medisimbio.backend.provider.dto;

import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight Summary DTO for Provider listings and search results.
 */
@Schema(description = "Provider summary payload for search listings")
public class ProviderSummaryDto {

    @Schema(description = "Provider UUID", example = "33333333-3333-3333-3333-333333333001")
    private UUID id;

    @Schema(description = "Full Name", example = "Dr. Rajesh Sharma")
    private String fullName;

    @Schema(description = "Medical council registration number", example = "MCI-2015-89412")
    private String professionalRegistrationNumber;

    @Schema(description = "Qualification", example = "MBBS, MD - Cardiology")
    private String qualification;

    @Schema(description = "Experience Years", example = "12")
    private Integer experienceYears;

    @Schema(description = "Profile Photo URL", example = "https://example.com/photos/dr-rajesh.jpg")
    private String profilePhotoUrl;

    @Schema(description = "Verification Status", example = "VERIFIED")
    private String verificationStatus;

    @Schema(description = "Active Status", example = "true")
    private boolean active;

    @Schema(description = "Specialties")
    private List<SpecialtyResponseDto> specialties = new ArrayList<>();

    public ProviderSummaryDto() {
    }

    public ProviderSummaryDto(UUID id, String fullName, String professionalRegistrationNumber,
                              String qualification, Integer experienceYears, String profilePhotoUrl,
                              String verificationStatus, boolean active, List<SpecialtyResponseDto> specialties) {
        this.id = id;
        this.fullName = fullName;
        this.professionalRegistrationNumber = professionalRegistrationNumber;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.profilePhotoUrl = profilePhotoUrl;
        this.verificationStatus = verificationStatus;
        this.active = active;
        this.specialties = specialties != null ? specialties : new ArrayList<>();
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
}
