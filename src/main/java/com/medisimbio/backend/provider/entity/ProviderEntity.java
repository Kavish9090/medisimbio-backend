package com.medisimbio.backend.provider.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a Healthcare Provider (Doctor / Medical Specialist).
 */
@Entity
@Table(name = "providers")
public class ProviderEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", length = 150, nullable = false)
    private String fullName;

    @Column(name = "professional_registration_number", length = 100, nullable = false, unique = true)
    private String professionalRegistrationNumber;

    @Column(name = "qualification", length = 200, nullable = false)
    private String qualification;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears = 0;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "mobile_number", length = 20, nullable = false, unique = true)
    private String mobileNumber;

    @Column(name = "verification_status", length = 50, nullable = false)
    private String verificationStatus = "VERIFIED";

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "auth_user_id", length = 128, unique = true)
    private String authUserId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "provider_specialties",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<SpecialtyEntity> specialties = new HashSet<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProviderHospitalEntity> hospitalAffiliations = new HashSet<>();

    public ProviderEntity() {
    }

    public ProviderEntity(UUID id, String fullName, String professionalRegistrationNumber,
                          String qualification, Integer experienceYears, String email, String mobileNumber) {
        this(id, null, fullName, professionalRegistrationNumber, qualification, experienceYears, email, mobileNumber);
    }

    public ProviderEntity(UUID id, String authUserId, String fullName, String professionalRegistrationNumber,
                          String qualification, Integer experienceYears, String email, String mobileNumber) {
        this.id = id != null ? id : UUID.randomUUID();
        this.authUserId = authUserId;
        this.fullName = fullName;
        this.professionalRegistrationNumber = professionalRegistrationNumber;
        this.qualification = qualification;
        this.experienceYears = experienceYears != null ? experienceYears : 0;
        this.email = email;
        this.mobileNumber = mobileNumber;
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
        this.experienceYears = experienceYears != null ? experienceYears : 0;
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

    public Set<SpecialtyEntity> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(Set<SpecialtyEntity> specialties) {
        this.specialties = specialties != null ? specialties : new HashSet<>();
    }

    public Set<ProviderHospitalEntity> getHospitalAffiliations() {
        return hospitalAffiliations;
    }

    public void setHospitalAffiliations(Set<ProviderHospitalEntity> hospitalAffiliations) {
        this.hospitalAffiliations = hospitalAffiliations != null ? hospitalAffiliations : new HashSet<>();
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public void addSpecialty(SpecialtyEntity specialty) {
        if (specialty != null) {
            this.specialties.add(specialty);
        }
    }

    public void removeSpecialty(SpecialtyEntity specialty) {
        if (specialty != null) {
            this.specialties.remove(specialty);
        }
    }
}
