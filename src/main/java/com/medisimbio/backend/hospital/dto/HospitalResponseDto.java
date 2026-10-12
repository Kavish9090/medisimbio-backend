package com.medisimbio.backend.hospital.dto;

import com.medisimbio.backend.hospital.entity.HospitalType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed Response DTO for Hospital / Facility.
 */
@Schema(description = "Hospital detailed response payload")
public class HospitalResponseDto {

    @Schema(description = "Hospital UUID", example = "22222222-2222-2222-2222-222222222001")
    private UUID id;

    @Schema(description = "Hospital name", example = "All India Institute of Medical Sciences (AIIMS)")
    private String name;

    @Schema(description = "Type of hospital", example = "GOVERNMENT")
    private HospitalType hospitalType;

    @Schema(description = "Registration identifier / ABDM ID", example = "HOSP-AIIMS-DEL-01")
    private String registrationIdentifier;

    @Schema(description = "Primary phone", example = "+911126588500")
    private String phone;

    @Schema(description = "Email", example = "info@aiims.edu")
    private String email;

    @Schema(description = "Address", example = "Sri Aurobindo Marg, Ansari Nagar")
    private String address;

    @Schema(description = "City", example = "New Delhi")
    private String city;

    @Schema(description = "State", example = "Delhi")
    private String state;

    @Schema(description = "Postal code", example = "110029")
    private String postalCode;

    @Schema(description = "Latitude", example = "28.5672")
    private Double latitude;

    @Schema(description = "Longitude", example = "77.2100")
    private Double longitude;

    @Schema(description = "Verification status", example = "VERIFIED")
    private String verificationStatus;

    @Schema(description = "Active status", example = "true")
    private boolean active;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record update timestamp")
    private Instant updatedAt;

    public HospitalResponseDto() {
    }

    public HospitalResponseDto(UUID id, String name, HospitalType hospitalType, String registrationIdentifier,
                               String phone, String email, String address, String city, String state,
                               String postalCode, Double latitude, Double longitude, String verificationStatus,
                               boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.hospitalType = hospitalType;
        this.registrationIdentifier = registrationIdentifier;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verificationStatus = verificationStatus;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HospitalType getHospitalType() {
        return hospitalType;
    }

    public void setHospitalType(HospitalType hospitalType) {
        this.hospitalType = hospitalType;
    }

    public String getRegistrationIdentifier() {
        return registrationIdentifier;
    }

    public void setRegistrationIdentifier(String registrationIdentifier) {
        this.registrationIdentifier = registrationIdentifier;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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
