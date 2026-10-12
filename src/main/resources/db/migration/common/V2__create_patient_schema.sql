-- =============================================================================
-- MediSimbio Database Migration: V2 Patient Management & Med-ID Schema
-- Description: Creates authoritative relational tables for Patient profiles
--              and Med-ID Authority with UUID primary keys and uniqueness constraints.
-- =============================================================================

-- 1. PATIENTS TABLE
CREATE TABLE patients (
    id UUID NOT NULL PRIMARY KEY,
    auth_user_id VARCHAR(128) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mobile_number VARCHAR(20) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    blood_group VARCHAR(10),
    allergies VARCHAR(500),
    existing_conditions VARCHAR(500),
    emergency_contact_name VARCHAR(150),
    emergency_contact_relationship VARCHAR(50),
    emergency_contact_mobile VARCHAR(20),
    photo_url VARCHAR(500),
    profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for high-frequency lookup fields
CREATE INDEX idx_patients_auth_user_id ON patients(auth_user_id);
CREATE INDEX idx_patients_email ON patients(email);
CREATE INDEX idx_patients_mobile ON patients(mobile_number);

-- 2. MED_IDS TABLE
CREATE TABLE med_ids (
    id UUID NOT NULL PRIMARY KEY,
    patient_id UUID NOT NULL UNIQUE REFERENCES patients(id) ON DELETE CASCADE,
    med_id_number VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    qr_token VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Med-ID lookups and patient relationship
CREATE INDEX idx_med_ids_patient_id ON med_ids(patient_id);
CREATE INDEX idx_med_ids_number ON med_ids(med_id_number);
