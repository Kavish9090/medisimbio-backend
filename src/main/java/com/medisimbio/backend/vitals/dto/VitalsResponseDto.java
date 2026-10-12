package com.medisimbio.backend.vitals.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Detailed representation of recorded physiological vitals.
 */
@Schema(description = "Physiological vitals response")
public class VitalsResponseDto {

    @Schema(description = "Vitals record UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Encounter UUID", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID encounterId;

    @Schema(description = "Encounter Reference", example = "ENC-20261008-7K2M")
    private String encounterReference;

    @Schema(description = "Patient UUID", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID patientId;

    @Schema(description = "Patient Full Name", example = "Rahul Sharma")
    private String patientName;

    @Schema(description = "Measurement timestamp")
    private Instant measuredAt;

    @Schema(description = "Temperature in Celsius or Fahrenheit", example = "98.6")
    private BigDecimal temperature;

    @Schema(description = "Heart rate (bpm)", example = "72")
    private Integer heartRate;

    @Schema(description = "Respiratory rate (breaths/min)", example = "16")
    private Integer respiratoryRate;

    @Schema(description = "Systolic blood pressure (mmHg)", example = "120")
    private Integer systolicBloodPressure;

    @Schema(description = "Diastolic blood pressure (mmHg)", example = "80")
    private Integer diastolicBloodPressure;

    @Schema(description = "Formatted blood pressure string", example = "120/80 mmHg")
    private String bloodPressureFormatted;

    @Schema(description = "Oxygen saturation percentage (SpO2)", example = "98.5")
    private BigDecimal oxygenSaturation;

    @Schema(description = "Weight in kg", example = "68.50")
    private BigDecimal weight;

    @Schema(description = "Height in cm", example = "175.00")
    private BigDecimal height;

    @Schema(description = "Body Mass Index (BMI)", example = "22.4")
    private BigDecimal bmi;

    @Schema(description = "Blood glucose (mg/dL)", example = "95.0")
    private BigDecimal bloodGlucose;

    @Schema(description = "Clinical notes on vitals", example = "Resting BP measured")
    private String notes;

    @Schema(description = "Provider or staff who recorded vitals", example = "auth_doctor_1")
    private String recordedBy;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record last modified timestamp")
    private Instant updatedAt;

    public VitalsResponseDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.encounterId = encounterId;
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

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(Instant measuredAt) {
        this.measuredAt = measuredAt;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Integer getSystolicBloodPressure() {
        return systolicBloodPressure;
    }

    public void setSystolicBloodPressure(Integer systolicBloodPressure) {
        this.systolicBloodPressure = systolicBloodPressure;
    }

    public Integer getDiastolicBloodPressure() {
        return diastolicBloodPressure;
    }

    public void setDiastolicBloodPressure(Integer diastolicBloodPressure) {
        this.diastolicBloodPressure = diastolicBloodPressure;
    }

    public String getBloodPressureFormatted() {
        return bloodPressureFormatted;
    }

    public void setBloodPressureFormatted(String bloodPressureFormatted) {
        this.bloodPressureFormatted = bloodPressureFormatted;
    }

    public BigDecimal getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setOxygenSaturation(BigDecimal oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public BigDecimal getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(BigDecimal bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
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
