-- =============================================================================
-- MediSimbio Database Migration (H2 Engine): V5 Harden Appointment Concurrency
-- Description: Composite index for appointment slot concurrency validation in
--              in-memory H2 test environments.
-- =============================================================================

CREATE INDEX uq_appointments_active_doctor_slot
ON appointments (provider_id, appointment_date, start_time, status);
