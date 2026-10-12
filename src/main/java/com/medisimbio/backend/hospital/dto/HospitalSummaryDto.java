package com.medisimbio.backend.hospital.dto;

import com.medisimbio.backend.hospital.entity.HospitalType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Lightweight Summary DTO for Hospital directory and search listings.
 */
@Schema(description = "Hospital summary payload for search listings")
public class HospitalSummaryDto {

    @Schema(description = "Hospital UUID", example = "22222222-2222-2222-2222-222222222001")
    private UUID id;

    @Schema(description = "Hospital name", example = "All India Institute of Medical Sciences (AIIMS)")
    private String name;

    @Schema(description = "Type of hospital", example = "GOVERNMENT")
    private HospitalType hospitalType;

    @Schema(description = "Primary phone", example = "+911126588500")
    private String phone;

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

    @Schema(description = "Active status", example = "true")
    private boolean active;

    public HospitalSummaryDto() {
    }

    public HospitalSummaryDto(UUID id, String name, HospitalType hospitalType, String phone,
                              String city, String state, String postalCode, Double latitude,
                              Double longitude, boolean active) {
        this.id = id;
        this.name = name;
        this.hospitalType = hospitalType;
        this.phone = phone;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
