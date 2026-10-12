package com.medisimbio.backend.prescription.entity;

/**
 * State lifecycle of an electronic prescription.
 *
 * DRAFT      -> Editable draft prepared by the prescribing provider. Not finalized, not visible on patient timeline.
 * FINALIZED  -> Sealed and authoritative clinical order. Immutable. Visible to patient and on medical timeline.
 * CANCELLED  -> Cancelled prescription. Preserved for clinical audit; cannot be dispensed.
 * SUPERSEDED -> Replaced by a corrected or amended prescription version through the replacement workflow.
 */
public enum PrescriptionStatus {
    DRAFT,
    FINALIZED,
    CANCELLED,
    SUPERSEDED;

    public boolean canEditItems() {
        return this == DRAFT;
    }

    public boolean canFinalize() {
        return this == DRAFT;
    }

    public boolean canCancel() {
        return this == DRAFT || this == FINALIZED;
    }

    public boolean canReplace() {
        return this == FINALIZED;
    }

    public boolean canCreateDispensingOrder() {
        return this == FINALIZED;
    }
}
