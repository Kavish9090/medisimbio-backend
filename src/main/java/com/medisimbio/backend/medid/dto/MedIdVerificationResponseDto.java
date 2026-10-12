package com.medisimbio.backend.medid.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.medisimbio.backend.medid.entity.MedIdStatus;

import java.time.Instant;

/**
 * Public response DTO for verifying a Med-ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedIdVerificationResponseDto {

    private String medId;
    private boolean valid;
    private MedIdStatus status;
    private String maskedPatientName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant issuedAt;

    public MedIdVerificationResponseDto() {
    }

    public MedIdVerificationResponseDto(String medId, boolean valid, MedIdStatus status, String maskedPatientName, Instant issuedAt) {
        this.medId = medId;
        this.valid = valid;
        this.status = status;
        this.maskedPatientName = maskedPatientName;
        this.issuedAt = issuedAt;
    }

    public String getMedId() {
        return medId;
    }

    public void setMedId(String medId) {
        this.medId = medId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public MedIdStatus getStatus() {
        return status;
    }

    public void setStatus(MedIdStatus status) {
        this.status = status;
    }

    public String getMaskedPatientName() {
        return maskedPatientName;
    }

    public void setMaskedPatientName(String maskedPatientName) {
        this.maskedPatientName = maskedPatientName;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
