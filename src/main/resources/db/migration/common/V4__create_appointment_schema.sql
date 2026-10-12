-- =============================================================================
-- MediSimbio Database Migration: V4 Doctor Availability & Appointment Schema
-- Description: Creates relational tables for Doctor Availabilities, Consultation
--              Appointments, and composite indexes for double-booking prevention.
-- =============================================================================

-- 1. DOCTOR AVAILABILITY TABLE
CREATE TABLE doctor_availabilities (
    id UUID NOT NULL PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL DEFAULT 30,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doc_avail_provider ON doctor_availabilities(provider_id);
CREATE INDEX idx_doc_avail_hospital ON doctor_availabilities(hospital_id);
CREATE INDEX idx_doc_avail_day ON doctor_availabilities(day_of_week);
CREATE INDEX idx_doc_avail_active ON doctor_availabilities(active);
CREATE INDEX idx_doc_avail_lookup ON doctor_availabilities(provider_id, hospital_id, day_of_week, active);

-- 2. APPOINTMENTS TABLE
CREATE TABLE appointments (
    id UUID NOT NULL PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE RESTRICT,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    specialty_id UUID REFERENCES specialties(id) ON DELETE SET NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    booking_reference VARCHAR(30) NOT NULL UNIQUE,
    visit_reason VARCHAR(500),
    cancellation_reason VARCHAR(500),
    rescheduled_from_id UUID REFERENCES appointments(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_provider ON appointments(provider_id);
CREATE INDEX idx_appointments_hospital ON appointments(hospital_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_ref ON appointments(booking_reference);

-- Composite indexes for high performance slot availability and collision validation
CREATE INDEX idx_appointments_doctor_slot ON appointments (provider_id, appointment_date, start_time, status);
CREATE INDEX idx_appointments_patient_slot ON appointments (patient_id, appointment_date, start_time, status);
