package com.medisimbio.backend.encounter.entity;

/**
 * State lifecycle of a clinical encounter.
 *
 * OPEN        -> Encounter created, waiting for consultation to start.
 * IN_PROGRESS -> Clinical consultation active with healthcare provider.
 * COMPLETED   -> Clinical interaction finalized and closed.
 * CANCELLED   -> Encounter cancelled.
 */
public enum EncounterStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canStart() {
        return this == OPEN;
    }

    public boolean canComplete() {
        return this == IN_PROGRESS;
    }

    public boolean canCancel() {
        return this == OPEN || this == IN_PROGRESS;
    }

    public boolean canModifyClinicalNotes() {
        return this == OPEN || this == IN_PROGRESS;
    }
}
