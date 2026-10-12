package com.medisimbio.backend.medid.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.medisimbio.backend.medid.entity.MedIdStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Public response DTO for Med-ID details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedIdResponseDto {

    private String medId;
    private UUID patientId;
    private String patientName;
    private MedIdStatus status;
    private String qrToken;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant issuedAt;

    public MedIdResponseDto() {
    }

    public MedIdResponseDto(String medId, UUID patientId, String patientName, MedIdStatus status, String qrToken, Instant issuedAt) {
        this.medId = medId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.status = status;
        this.qrToken = qrToken;
        this.issuedAt = issuedAt;
    }

    public String getMedId() {
        return medId;
    }

    public void setMedId(String medId) {
        this.medId = medId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public MedIdStatus getStatus() {
        return status;
    }

    public void setStatus(MedIdStatus status) {
        this.status = status;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
