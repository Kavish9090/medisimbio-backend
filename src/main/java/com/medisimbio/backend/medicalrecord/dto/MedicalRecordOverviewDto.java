package com.medisimbio.backend.medicalrecord.dto;

import com.medisimbio.backend.diagnosis.dto.DiagnosisResponseDto;
import com.medisimbio.backend.encounter.dto.EncounterSummaryDto;
import com.medisimbio.backend.patient.dto.PatientResponseDto;
import com.medisimbio.backend.vitals.dto.VitalsResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Consolidated medical record overview aggregating clinical encounters, active diagnoses, and latest vitals.
 */
@Schema(description = "Aggregated patient medical record overview")
public class MedicalRecordOverviewDto {

    @Schema(description = "Patient profile and demographic information")
    private PatientResponseDto patient;

    @Schema(description = "Recent clinical encounters")
    private List<EncounterSummaryDto> encounters;

    @Schema(description = "Current active clinical diagnoses")
    private List<DiagnosisResponseDto> activeDiagnoses;

    @Schema(description = "Most recently recorded physiological vitals")
    private VitalsResponseDto latestVitals;

    @Schema(description = "Total number of clinical encounters recorded", example = "5")
    private long totalEncountersCount;

    @Schema(description = "Total number of diagnoses recorded", example = "3")
    private long totalDiagnosesCount;

    public MedicalRecordOverviewDto() {
    }

    public MedicalRecordOverviewDto(PatientResponseDto patient, List<EncounterSummaryDto> encounters,
                                  List<DiagnosisResponseDto> activeDiagnoses, VitalsResponseDto latestVitals,
                                  long totalEncountersCount, long totalDiagnosesCount) {
        this.patient = patient;
        this.encounters = encounters;
        this.activeDiagnoses = activeDiagnoses;
        this.latestVitals = latestVitals;
        this.totalEncountersCount = totalEncountersCount;
        this.totalDiagnosesCount = totalDiagnosesCount;
    }

    public PatientResponseDto getPatient() {
        return patient;
    }

    public void setPatient(PatientResponseDto patient) {
        this.patient = patient;
    }

    public List<EncounterSummaryDto> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<EncounterSummaryDto> encounters) {
        this.encounters = encounters;
    }

    public List<DiagnosisResponseDto> getActiveDiagnoses() {
        return activeDiagnoses;
    }

    public void setActiveDiagnoses(List<DiagnosisResponseDto> activeDiagnoses) {
        this.activeDiagnoses = activeDiagnoses;
    }

    public VitalsResponseDto getLatestVitals() {
        return latestVitals;
    }

    public void setLatestVitals(VitalsResponseDto latestVitals) {
        this.latestVitals = latestVitals;
    }

    public long getTotalEncountersCount() {
        return totalEncountersCount;
    }

    public void setTotalEncountersCount(long totalEncountersCount) {
        this.totalEncountersCount = totalEncountersCount;
    }

    public long getTotalDiagnosesCount() {
        return totalDiagnosesCount;
    }

    public void setTotalDiagnosesCount(long totalDiagnosesCount) {
        this.totalDiagnosesCount = totalDiagnosesCount;
    }
}
