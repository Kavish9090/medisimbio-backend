package com.medisimbio.backend.encounter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for modifying clinical notes, findings, and plans of an active encounter.
 */
@Schema(description = "Request DTO for updating clinical notes and treatment plan of an encounter")
public class UpdateEncounterRequestDto {

    @Size(max = 500, message = "Chief complaint must not exceed 500 characters")
    @Schema(description = "Updated chief complaint", example = "Persistent high fever, dry cough, and fatigue")
    private String chiefComplaint;

    @Schema(description = "Detailed provider clinical examination notes and observations", example = "Patient appears alert. Chest clear to auscultation bilaterally. Throat mildly erythematous.")
    private String clinicalNotes;

    @Size(max = 500, message = "Diagnosis summary must not exceed 500 characters")
    @Schema(description = "Brief summary of clinical diagnosis", example = "Acute upper respiratory tract infection")
    private String diagnosisSummary;

    @Schema(description = "Prescribed clinical treatment plan, medications, and advice", example = "Adequate hydration, Paracetamol 500mg TDS for 3 days, steam inhalation.")
    private String treatmentPlan;

    @Size(max = 500, message = "Follow-up instructions must not exceed 500 characters")
    @Schema(description = "Post-consultation instructions or next review date", example = "Review in 5 days if symptoms persist or fever exceeds 102F")
    private String followUpInstructions;

    public UpdateEncounterRequestDto() {
    }

    public UpdateEncounterRequestDto(String chiefComplaint, String clinicalNotes, String diagnosisSummary,
                                     String treatmentPlan, String followUpInstructions) {
        this.chiefComplaint = chiefComplaint;
        this.clinicalNotes = clinicalNotes;
        this.diagnosisSummary = diagnosisSummary;
        this.treatmentPlan = treatmentPlan;
        this.followUpInstructions = followUpInstructions;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public String getDiagnosisSummary() {
        return diagnosisSummary;
    }

    public void setDiagnosisSummary(String diagnosisSummary) {
        this.diagnosisSummary = diagnosisSummary;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getFollowUpInstructions() {
        return followUpInstructions;
    }

    public void setFollowUpInstructions(String followUpInstructions) {
        this.followUpInstructions = followUpInstructions;
    }
}
