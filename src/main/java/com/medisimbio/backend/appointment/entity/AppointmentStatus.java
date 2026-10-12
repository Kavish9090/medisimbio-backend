package com.medisimbio.backend.appointment.entity;

/**
 * Lifecycle states for consultation appointments.
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    NO_SHOW;

    public boolean isTerminal() {
        return this == CANCELLED || this == COMPLETED || this == NO_SHOW;
    }

    public boolean canCancel() {
        return this == SCHEDULED || this == CONFIRMED;
    }

    public boolean canReschedule() {
        return this == SCHEDULED || this == CONFIRMED;
    }
}
