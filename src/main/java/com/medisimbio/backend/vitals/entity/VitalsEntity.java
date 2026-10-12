package com.medisimbio.backend.vitals.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing physiological vitals measurements recorded during an encounter.
 */
@Entity
@Table(name = "vitals")
public class VitalsEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encounter_id", nullable = false)
    private EncounterEntity encounter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt = Instant.now();

    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "systolic_blood_pressure")
    private Integer systolicBloodPressure;

    @Column(name = "diastolic_blood_pressure")
    private Integer diastolicBloodPressure;

    @Column(name = "oxygen_saturation", precision = 4, scale = 1)
    private BigDecimal oxygenSaturation;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal height;

    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(name = "blood_glucose", precision = 5, scale = 1)
    private BigDecimal bloodGlucose;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public VitalsEntity() {
    }

    public VitalsEntity(UUID id, EncounterEntity encounter, PatientEntity patient, Instant measuredAt,
                        BigDecimal temperature, Integer heartRate, Integer respiratoryRate,
                        Integer systolicBloodPressure, Integer diastolicBloodPressure,
                        BigDecimal oxygenSaturation, BigDecimal weight, BigDecimal height,
                        BigDecimal bmi, BigDecimal bloodGlucose, String notes, String recordedBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.encounter = encounter;
        this.patient = patient;
        this.measuredAt = measuredAt != null ? measuredAt : Instant.now();
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
        this.recordedBy = recordedBy;
        this.createdBy = recordedBy;
        this.updatedBy = recordedBy;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.measuredAt == null) {
            this.measuredAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EncounterEntity getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterEntity encounter) {
        this.encounter = encounter;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
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

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
