package com.medisimbio.backend.hospital.dto;

import com.medisimbio.backend.hospital.entity.HospitalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request payload for creating a hospital or clinic facility.
 */
@Schema(description = "Request payload for registering a hospital")
public class CreateHospitalRequestDto {

    @Schema(description = "Hospital name", example = "All India Institute of Medical Sciences (AIIMS)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Hospital name is required")
    @Size(max = 200, message = "Hospital name cannot exceed 200 characters")
    private String name;

    @Schema(description = "Type of hospital", example = "GOVERNMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hospital type is required")
    private HospitalType hospitalType;

    @Schema(description = "Official registry identifier / ABDM ID", example = "HOSP-AIIMS-DEL-01")
    @Size(max = 100, message = "Registration identifier cannot exceed 100 characters")
    private String registrationIdentifier;

    @Schema(description = "Primary contact phone", example = "+911126588500", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Schema(description = "Official email address", example = "info@aiims.edu")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Physical address", example = "Sri Aurobindo Marg, Ansari Nagar", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Address is required")
    @Size(max = 300, message = "Address cannot exceed 300 characters")
    private String address;

    @Schema(description = "City", example = "New Delhi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Schema(description = "State", example = "Delhi", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Schema(description = "Postal code / PIN code", example = "110029", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Schema(description = "GPS Latitude coordinate", example = "28.5672")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @Schema(description = "GPS Longitude coordinate", example = "77.2100")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    public CreateHospitalRequestDto() {
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
}
