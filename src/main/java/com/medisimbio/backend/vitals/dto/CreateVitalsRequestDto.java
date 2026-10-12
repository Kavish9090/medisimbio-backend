package com.medisimbio.backend.vitals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request payload for recording physiological vitals during a clinical encounter.
 */
@Schema(description = "Request DTO for recording physiological vitals")
public class CreateVitalsRequestDto {

    @Schema(description = "Measurement timestamp. Defaults to now if omitted.")
    private Instant measuredAt;

    @DecimalMin(value = "0.0", message = "Temperature cannot be negative")
    @Schema(description = "Body temperature in Celsius or Fahrenheit", example = "98.6")
    private BigDecimal temperature;

    @Min(value = 0, message = "Heart rate cannot be negative")
    @Schema(description = "Heart rate / pulse in beats per minute (bpm)", example = "72")
    private Integer heartRate;

    @Min(value = 0, message = "Respiratory rate cannot be negative")
    @Schema(description = "Respiratory rate in breaths per minute", example = "16")
    private Integer respiratoryRate;

    @Min(value = 1, message = "Systolic blood pressure must be positive")
    @Schema(description = "Systolic blood pressure in mmHg", example = "120")
    private Integer systolicBloodPressure;

    @Min(value = 1, message = "Diastolic blood pressure must be positive")
    @Schema(description = "Diastolic blood pressure in mmHg", example = "80")
    private Integer diastolicBloodPressure;

    @DecimalMin(value = "0.0", message = "Oxygen saturation must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Oxygen saturation must be between 0 and 100")
    @Schema(description = "Blood oxygen saturation percentage (SpO2)", example = "98.5")
    private BigDecimal oxygenSaturation;

    @DecimalMin(value = "0.01", message = "Weight must be positive")
    @Schema(description = "Body weight in kilograms (kg)", example = "68.50")
    private BigDecimal weight;

    @DecimalMin(value = "0.01", message = "Height must be positive")
    @Schema(description = "Body height in centimeters (cm)", example = "175.00")
    private BigDecimal height;

    @Schema(description = "Body Mass Index (BMI). Calculated automatically if omitted and height/weight provided.", example = "22.4")
    private BigDecimal bmi;

    @DecimalMin(value = "0.0", message = "Blood glucose cannot be negative")
    @Schema(description = "Blood glucose level in mg/dL", example = "95.0")
    private BigDecimal bloodGlucose;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Additional observations on vital measurements", example = "Patient was resting for 10 minutes before BP measurement.")
    private String notes;

    public CreateVitalsRequestDto() {
    }

    public CreateVitalsRequestDto(Instant measuredAt, BigDecimal temperature, Integer heartRate,
                                 Integer respiratoryRate, Integer systolicBloodPressure,
                                 Integer diastolicBloodPressure, BigDecimal oxygenSaturation,
                                 BigDecimal weight, BigDecimal height, BigDecimal bmi,
                                 BigDecimal bloodGlucose, String notes) {
        this.measuredAt = measuredAt;
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.systolicBloodPressure = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
        this.oxygenSaturation = oxygenSaturation;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.bloodGlucose = bloodGlucose;
        this.notes = notes;
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
}
