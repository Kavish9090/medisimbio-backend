package com.medisimbio.backend.diagnosis.dto;

import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import com.medisimbio.backend.diagnosis.entity.DiagnosisType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for modifying status, codes, or notes on an existing clinical diagnosis.
 */
@Schema(description = "Request DTO for updating an existing clinical diagnosis")
public class UpdateDiagnosisRequestDto {

    @Size(max = 255, message = "Diagnosis name must not exceed 255 characters")
    @Schema(description = "Updated diagnosis name", example = "Acute Bronchitis (Resolving)")
    private String diagnosisName;

    @Size(max = 50, message = "Diagnosis code must not exceed 50 characters")
    @Schema(description = "Updated ICD code", example = "J20.9")
    private String diagnosisCode;

    @Size(max = 1000, message = "Diagnosis description must not exceed 1000 characters")
    @Schema(description = "Updated clinical description")
    private String diagnosisDescription;

    @Schema(description = "Updated diagnosis classification type", example = "SECONDARY")
    private DiagnosisType diagnosisType;

    @Schema(description = "Updated diagnosis status", example = "RESOLVED")
    private DiagnosisStatus status;

    @Schema(description = "Updated clinical notes", example = "Patient reports cough subsided. Lungs clear on follow-up.")
    private String notes;

    public UpdateDiagnosisRequestDto() {
    }

    public UpdateDiagnosisRequestDto(String diagnosisName, String diagnosisCode, String diagnosisDescription,
                                     DiagnosisType diagnosisType, DiagnosisStatus status, String notes) {
        this.diagnosisName = diagnosisName;
        this.diagnosisCode = diagnosisCode;
        this.diagnosisDescription = diagnosisDescription;
        this.diagnosisType = diagnosisType;
        this.status = status;
        this.notes = notes;
    }

    public String getDiagnosisName() {
        return diagnosisName;
    }

    public void setDiagnosisName(String diagnosisName) {
        this.diagnosisName = diagnosisName;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisDescription() {
        return diagnosisDescription;
    }

    public void setDiagnosisDescription(String diagnosisDescription) {
        this.diagnosisDescription = diagnosisDescription;
    }

    public DiagnosisType getDiagnosisType() {
        return diagnosisType;
    }

    public void setDiagnosisType(DiagnosisType diagnosisType) {
        this.diagnosisType = diagnosisType;
    }

    public DiagnosisStatus getStatus() {
        return status;
    }

    public void setStatus(DiagnosisStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
