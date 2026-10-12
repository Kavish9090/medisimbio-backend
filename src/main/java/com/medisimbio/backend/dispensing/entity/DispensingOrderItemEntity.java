package com.medisimbio.backend.dispensing.entity;

import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an individual medicine item within a dispensing order.
 */
@Entity
@Table(name = "dispensing_order_items")
public class DispensingOrderItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispensing_order_id", nullable = false)
    private DispensingOrderEntity dispensingOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_item_id", nullable = false)
    private PrescriptionItemEntity prescriptionItem;

    @Column(name = "medicine_name", length = 255, nullable = false)
    private String medicineName;

    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "dispensed_quantity", nullable = false)
    private Integer dispensedQuantity = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public DispensingOrderItemEntity() {
    }

    public DispensingOrderItemEntity(UUID id, DispensingOrderEntity dispensingOrder,
                                     PrescriptionItemEntity prescriptionItem, String medicineName,
                                     Integer requestedQuantity) {
        this(id, dispensingOrder, prescriptionItem, medicineName, requestedQuantity, 0);
    }

    public DispensingOrderItemEntity(UUID id, DispensingOrderEntity dispensingOrder,
                                     PrescriptionItemEntity prescriptionItem, String medicineName,
                                     Integer requestedQuantity, Integer dispensedQuantity) {
        this.id = id != null ? id : UUID.randomUUID();
        this.dispensingOrder = dispensingOrder;
        this.prescriptionItem = prescriptionItem;
        this.medicineName = medicineName;
        this.requestedQuantity = requestedQuantity;
        this.dispensedQuantity = dispensedQuantity != null ? dispensedQuantity : 0;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DispensingOrderEntity getDispensingOrder() {
        return dispensingOrder;
    }

    public void setDispensingOrder(DispensingOrderEntity dispensingOrder) {
        this.dispensingOrder = dispensingOrder;
    }

    public PrescriptionItemEntity getPrescriptionItem() {
        return prescriptionItem;
    }

    public void setPrescriptionItem(PrescriptionItemEntity prescriptionItem) {
        this.prescriptionItem = prescriptionItem;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getDispensedQuantity() {
        return dispensedQuantity;
    }

    public void setDispensedQuantity(Integer dispensedQuantity) {
        this.dispensedQuantity = dispensedQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
