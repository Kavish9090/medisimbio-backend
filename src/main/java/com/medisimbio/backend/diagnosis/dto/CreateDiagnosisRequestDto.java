package com.medisimbio.backend.diagnosis.dto;

import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import com.medisimbio.backend.diagnosis.entity.DiagnosisType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Request payload for adding a provider-entered clinical diagnosis to an encounter.
 */
@Schema(description = "Request DTO for recording a clinical diagnosis")
public class CreateDiagnosisRequestDto {

    @NotBlank(message = "Diagnosis name is mandatory")
    @Size(max = 255, message = "Diagnosis name must not exceed 255 characters")
    @Schema(description = "Medical diagnosis title/name", example = "Acute Bronchitis", requiredMode = Schema.RequiredMode.REQUIRED)
    private String diagnosisName;

    @Size(max = 50, message = "Diagnosis code must not exceed 50 characters")
    @Schema(description = "Standard ICD-10 or clinical code", example = "J20.9")
    private String diagnosisCode;

    @Size(max = 1000, message = "Diagnosis description must not exceed 1000 characters")
    @Schema(description = "Detailed clinical description", example = "Acute inflammation of tracheobronchial tree with productive cough")
    private String diagnosisDescription;

    @Schema(description = "Diagnosis classification type", example = "PRIMARY")
    private DiagnosisType diagnosisType = DiagnosisType.PRIMARY;

    @Schema(description = "Initial diagnosis status", example = "ACTIVE")
    private DiagnosisStatus status = DiagnosisStatus.ACTIVE;

    @Schema(description = "Clinical diagnosis timestamp. Defaults to now if omitted.")
    private Instant diagnosedAt;

    @Schema(description = "Clinical notes or rationale", example = "Crackles heard on auscultation. CXR recommended if symptoms worsen.")
    private String notes;

    public CreateDiagnosisRequestDto() {
    }

    public CreateDiagnosisRequestDto(String diagnosisName, String diagnosisCode, String diagnosisDescription,
                                     DiagnosisType diagnosisType, DiagnosisStatus status, Instant diagnosedAt, String notes) {
        this.diagnosisName = diagnosisName;
        this.diagnosisCode = diagnosisCode;
        this.diagnosisDescription = diagnosisDescription;
        this.diagnosisType = diagnosisType != null ? diagnosisType : DiagnosisType.PRIMARY;
        this.status = status != null ? status : DiagnosisStatus.ACTIVE;
        this.diagnosedAt = diagnosedAt;
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

    public Instant getDiagnosedAt() {
        return diagnosedAt;
    }

    public void setDiagnosedAt(Instant diagnosedAt) {
        this.diagnosedAt = diagnosedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
