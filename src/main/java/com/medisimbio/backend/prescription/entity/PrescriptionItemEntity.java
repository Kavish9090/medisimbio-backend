package com.medisimbio.backend.prescription.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entity representing an individual medicine item within a prescription.
 */
@Entity
@Table(name = "prescription_items")
public class PrescriptionItemEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PrescriptionEntity prescription;

    @Column(name = "medicine_name", length = 255, nullable = false)
    private String medicineName;

    @Column(name = "active_ingredient", length = 255)
    private String activeIngredient;

    @Column(name = "strength", length = 100)
    private String strength;

    @Column(name = "dosage_form", length = 50)
    private String dosageForm;

    @Column(name = "dose", length = 100, nullable = false)
    private String dose;

    @Column(name = "route", length = 50, nullable = false)
    private String route;

    @Column(name = "frequency", length = 50, nullable = false)
    private String frequency;

    @Column(name = "duration_value")
    private Integer durationValue;

    @Column(name = "duration_unit", length = 20)
    private String durationUnit;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "quantity_unit", length = 50)
    private String quantityUnit;

    @Column(name = "refills_allowed", nullable = false)
    private Integer refillsAllowed = 0;

    @Column(name = "instructions", length = 500)
    private String instructions;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 1;

    public PrescriptionItemEntity() {
    }

    public PrescriptionItemEntity(UUID id, PrescriptionEntity prescription, String medicineName,
                                  String activeIngredient, String strength, String dosageForm,
                                  String dose, String route, String frequency, Integer durationValue,
                                  String durationUnit, Integer quantity, String quantityUnit,
                                  Integer refillsAllowed, String instructions, Integer sequenceNumber) {
        this.id = id != null ? id : UUID.randomUUID();
        this.prescription = prescription;
        this.medicineName = medicineName;
        this.activeIngredient = activeIngredient;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.dose = dose;
        this.route = route;
        this.frequency = frequency;
        this.durationValue = durationValue;
        this.durationUnit = durationUnit;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
        this.refillsAllowed = refillsAllowed != null ? refillsAllowed : 0;
        this.instructions = instructions;
        this.sequenceNumber = sequenceNumber != null ? sequenceNumber : 1;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.refillsAllowed == null) {
            this.refillsAllowed = 0;
        }
        if (this.sequenceNumber == null) {
            this.sequenceNumber = 1;
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PrescriptionEntity getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionEntity prescription) {
        this.prescription = prescription;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }

    public void setActiveIngredient(String activeIngredient) {
        this.activeIngredient = activeIngredient;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(Integer durationValue) {
        this.durationValue = durationValue;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public Integer getRefillsAllowed() {
        return refillsAllowed;
    }

    public void setRefillsAllowed(Integer refillsAllowed) {
        this.refillsAllowed = refillsAllowed;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
