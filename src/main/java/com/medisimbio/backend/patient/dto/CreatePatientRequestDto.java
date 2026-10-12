package com.medisimbio.backend.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medisimbio.backend.patient.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request payload for creating a new patient profile.
 */
public class CreatePatientRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Mobile number must be a valid phone number format (7 to 15 digits, optional + prefix)")
    private String mobileNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Size(max = 10, message = "Blood group must not exceed 10 characters")
    private String bloodGroup;

    @Size(max = 500, message = "Allergies summary must not exceed 500 characters")
    private String allergies;

    @Size(max = 500, message = "Existing conditions summary must not exceed 500 characters")
    private String existingConditions;

    @Size(max = 150, message = "Emergency contact name must not exceed 150 characters")
    private String emergencyContactName;

    @Size(max = 500, message = "Emergency contact relationship must not exceed 50 characters")
    private String emergencyContactRelationship;

    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "Emergency contact mobile must be a valid phone number format")
    private String emergencyContactMobile;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    public CreatePatientRequestDto() {
    }

    public CreatePatientRequestDto(String fullName, String email, String mobileNumber, LocalDate dateOfBirth, Gender gender) {
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getExistingConditions() {
        return existingConditions;
    }

    public void setExistingConditions(String existingConditions) {
        this.existingConditions = existingConditions;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }

    public String getEmergencyContactMobile() {
        return emergencyContactMobile;
    }

    public void setEmergencyContactMobile(String emergencyContactMobile) {
        this.emergencyContactMobile = emergencyContactMobile;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
