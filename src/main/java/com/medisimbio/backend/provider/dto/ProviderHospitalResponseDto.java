package com.medisimbio.backend.provider.dto;

import com.medisimbio.backend.hospital.dto.HospitalSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for Provider-Hospital Affiliation.
 */
@Schema(description = "Provider and Hospital affiliation response payload")
public class ProviderHospitalResponseDto {

    @Schema(description = "Affiliation UUID")
    private UUID id;

    @Schema(description = "Provider UUID")
    private UUID providerId;

    @Schema(description = "Provider Full Name")
    private String providerName;

    @Schema(description = "Hospital Details")
    private HospitalSummaryDto hospital;

    @Schema(description = "Department Name")
    private String department;

    @Schema(description = "Reference Consultation Fee")
    private BigDecimal consultationFee;

    @Schema(description = "Affiliation active status")
    private boolean active;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Update timestamp")
    private Instant updatedAt;

    public ProviderHospitalResponseDto() {
    }

    public ProviderHospitalResponseDto(UUID id, UUID providerId, String providerName,
                                       HospitalSummaryDto hospital, String department,
                                       BigDecimal consultationFee, boolean active,
                                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.providerId = providerId;
        this.providerName = providerName;
        this.hospital = hospital;
        this.department = department;
        this.consultationFee = consultationFee;
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

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public HospitalSummaryDto getHospital() {
        return hospital;
    }

    public void setHospital(HospitalSummaryDto hospital) {
        this.hospital = hospital;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
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
