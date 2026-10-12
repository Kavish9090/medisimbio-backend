package com.medisimbio.backend.provider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for associating a hospital with a healthcare provider.
 */
@Schema(description = "Request payload for affiliating a provider with a hospital")
public class AssignHospitalRequestDto {

    @Schema(description = "Hospital UUID", example = "22222222-2222-2222-2222-222222222001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hospital ID is required")
    private UUID hospitalId;

    @Schema(description = "Clinical department name", example = "Cardiology & Intensive Care")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String department;

    @Schema(description = "Reference consultation fee (non-payment placeholder)", example = "800.00")
    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    @Schema(description = "Active status of the affiliation", example = "true")
    private Boolean active = true;

    public AssignHospitalRequestDto() {
    }

    public AssignHospitalRequestDto(UUID hospitalId, String department, BigDecimal consultationFee, Boolean active) {
        this.hospitalId = hospitalId;
        this.department = department;
        this.consultationFee = consultationFee;
        this.active = active != null ? active : true;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
