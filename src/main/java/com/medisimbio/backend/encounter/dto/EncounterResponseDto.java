package com.medisimbio.backend.encounter.dto;

import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed response representation of a clinical encounter.
 */
@Schema(description = "Detailed clinical encounter response")
public class EncounterResponseDto {

    @Schema(description = "Encounter UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Human-readable unique encounter reference", example = "ENC-20261008-7K2M")
    private String encounterReference;

    @Schema(description = "Patient UUID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID patientId;

    @Schema(description = "Patient Full Name", example = "Rahul Sharma")
    private String patientName;

    @Schema(description = "Patient Med-ID Number", example = "MD-9842-1084")
    private String patientMedId;

    @Schema(description = "Provider UUID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID providerId;

    @Schema(description = "Provider Full Name", example = "Dr. Ananya Sharma")
    private String providerName;

    @Schema(description = "Hospital Facility UUID", example = "123e4567-e89b-12d3-a456-426614174003")
    private UUID hospitalId;

    @Schema(description = "Hospital Facility Name", example = "Apollo Multispeciality Hospital")
    private String hospitalName;

    @Schema(description = "Linked Appointment UUID", example = "123e4567-e89b-12d3-a456-426614174004")
    private UUID appointmentId;

    @Schema(description = "Linked Appointment Booking Reference", example = "MSB-20261008-8821")
    private String bookingReference;

    @Schema(description = "Encounter Type", example = "INITIAL_CONSULTATION")
    private EncounterType encounterType;

    @Schema(description = "Encounter Lifecycle Status", example = "IN_PROGRESS")
    private EncounterStatus status;

    @Schema(description = "Consultation start timestamp")
    private Instant startedAt;

    @Schema(description = "Consultation completion timestamp")
    private Instant endedAt;

    @Schema(description = "Chief presenting complaint", example = "Persistent high fever and headache for 3 days")
    private String chiefComplaint;

    @Schema(description = "Clinical examination and provider notes", example = "Throat mildly erythematous. Chest clear.")
    private String clinicalNotes;

    @Schema(description = "Diagnosis summary", example = "Acute upper respiratory tract infection")
    private String diagnosisSummary;

    @Schema(description = "Prescribed clinical treatment plan", example = "Paracetamol 500mg TDS for 3 days.")
    private String treatmentPlan;

    @Schema(description = "Post-consultation follow-up instructions", example = "Review in 5 days if fever persists")
    private String followUpInstructions;

    @Schema(description = "Identity of the provider or user who recorded this encounter", example = "auth_doctor_1")
    private String createdBy;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record last modified timestamp")
    private Instant updatedAt;

    public EncounterResponseDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEncounterReference() {
        return encounterReference;
    }

    public void setEncounterReference(String encounterReference) {
        this.encounterReference = encounterReference;
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

    public String getPatientMedId() {
        return patientMedId;
    }

    public void setPatientMedId(String patientMedId) {
        this.patientMedId = patientMedId;
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

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public EncounterStatus getStatus() {
        return status;
    }

    public void setStatus(EncounterStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
