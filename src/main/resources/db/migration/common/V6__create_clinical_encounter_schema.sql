-- =============================================================================
-- MediSimbio Database Migration: V6 Create Clinical Encounter & Medical Record Core
-- Description: Creates relational tables for Clinical Encounters, Diagnoses,
--              Vitals measurements, and indexes for medical timeline aggregation.
-- =============================================================================

-- 1. CLINICAL ENCOUNTERS TABLE
CREATE TABLE encounters (
    id UUID NOT NULL PRIMARY KEY,
    encounter_reference VARCHAR(30) NOT NULL UNIQUE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE RESTRICT,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE RESTRICT UNIQUE,
    encounter_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    chief_complaint VARCHAR(500),
    clinical_notes TEXT,
    diagnosis_summary VARCHAR(500),
    treatment_plan TEXT,
    follow_up_instructions VARCHAR(500),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_encounters_patient ON encounters(patient_id, started_at);
CREATE INDEX idx_encounters_provider ON encounters(provider_id, started_at);
CREATE INDEX idx_encounters_hospital ON encounters(hospital_id, started_at);
CREATE INDEX idx_encounters_appointment ON encounters(appointment_id);
CREATE INDEX idx_encounters_status ON encounters(status);
CREATE INDEX idx_encounters_ref ON encounters(encounter_reference);

-- 2. CLINICAL DIAGNOSES TABLE
CREATE TABLE diagnoses (
    id UUID NOT NULL PRIMARY KEY,
    encounter_id UUID NOT NULL CONSTRAINT diagnoses_encounter_id_fkey REFERENCES encounters(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    diagnosis_code VARCHAR(50),
    diagnosis_name VARCHAR(255) NOT NULL,
    diagnosis_description VARCHAR(1000),
    diagnosis_type VARCHAR(30) NOT NULL DEFAULT 'PRIMARY',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    diagnosed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    recorded_by VARCHAR(100),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_diagnoses_encounter ON diagnoses(encounter_id);
CREATE INDEX idx_diagnoses_patient ON diagnoses(patient_id, diagnosed_at);
CREATE INDEX idx_diagnoses_status ON diagnoses(status);

-- 3. PHYSIOLOGICAL VITALS TABLE
CREATE TABLE vitals (
    id UUID NOT NULL PRIMARY KEY,
    encounter_id UUID NOT NULL CONSTRAINT vitals_encounter_id_fkey REFERENCES encounters(id) ON DELETE CASCADE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    measured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    temperature NUMERIC(4, 1),
    heart_rate INTEGER,
    respiratory_rate INTEGER,
    systolic_blood_pressure INTEGER,
    diastolic_blood_pressure INTEGER,
    oxygen_saturation NUMERIC(4, 1),
    weight_kg NUMERIC(5, 2),
    height_cm NUMERIC(5, 2),
    bmi NUMERIC(4, 1),
    blood_glucose NUMERIC(5, 1),
    notes VARCHAR(500),
    recorded_by VARCHAR(100),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vitals_encounter ON vitals(encounter_id);
CREATE INDEX idx_vitals_patient ON vitals(patient_id, measured_at);
