-- =============================================================================
-- MediSimbio Database Migration: V5 Harden Appointment Concurrency
-- Description: Adds database-authoritative partial unique index for active
--              appointment slots (provider_id, appointment_date, start_time)
--              when status is SCHEDULED or CONFIRMED.
-- =============================================================================

-- 1. Create partial unique index on appointments for active slots.
-- This ensures that no doctor can be double-booked for the same start time on the
-- same calendar date across any number of concurrent backend instances.
-- Cancelled, completed, or no-show appointments do not occupy the unique slot,
-- allowing released slots to be immediately re-booked or rescheduled into.
CREATE UNIQUE INDEX uq_appointments_active_doctor_slot
ON appointments (provider_id, appointment_date, start_time)
WHERE status IN ('SCHEDULED', 'CONFIRMED');
