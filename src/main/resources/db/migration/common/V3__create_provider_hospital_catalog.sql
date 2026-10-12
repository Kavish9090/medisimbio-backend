-- =============================================================================
-- MediSimbio Database Migration: V3 Healthcare Provider & Hospital Catalog Schema
-- Description: Creates relational tables for Hospitals (Govt/Private), Medical
--              Specialties, Healthcare Providers (Doctors), and many-to-many
--              association tables with indexes and constraints.
-- =============================================================================

-- 1. SPECIALTIES TABLE
CREATE TABLE specialties (
    id UUID NOT NULL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_specialties_code ON specialties(code);
CREATE INDEX idx_specialties_active ON specialties(active);

-- Seed Essential Medical Specialties
INSERT INTO specialties (id, code, display_name, description, active) VALUES
    ('11111111-1111-1111-1111-111111111001', 'GENERAL_MEDICINE', 'General Medicine', 'Primary adult care, diagnosis and non-surgical treatment of chronic and acute illnesses', TRUE),
    ('11111111-1111-1111-1111-111111111002', 'CARDIOLOGY', 'Cardiology', 'Disorders of the heart and the cardiovascular system', TRUE),
    ('11111111-1111-1111-1111-111111111003', 'DERMATOLOGY', 'Dermatology', 'Diagnosis and treatment of skin, hair, and nail conditions', TRUE),
    ('11111111-1111-1111-1111-111111111004', 'PEDIATRICS', 'Pediatrics', 'Medical care for infants, children, and adolescents', TRUE),
    ('11111111-1111-1111-1111-111111111005', 'ORTHOPEDICS', 'Orthopedics', 'Musculoskeletal system disorders, bones, joints, ligaments, and tendons', TRUE),
    ('11111111-1111-1111-1111-111111111006', 'NEUROLOGY', 'Neurology', 'Disorders of the nervous system, brain, and spinal cord', TRUE),
    ('11111111-1111-1111-1111-111111111007', 'GYNECOLOGY', 'Gynecology & Obstetrics', 'Female reproductive health, pregnancy, and childbirth management', TRUE),
    ('11111111-1111-1111-1111-111111111008', 'OPHTHALMOLOGY', 'Ophthalmology', 'Eye and vision care, diagnostic examinations and surgical treatments', TRUE),
    ('11111111-1111-1111-1111-111111111009', 'ENT', 'ENT (Otolaryngology)', 'Ear, nose, throat, head and neck medical conditions', TRUE),
    ('11111111-1111-1111-1111-111111111010', 'PSYCHIATRY', 'Psychiatry', 'Mental, emotional, and behavioral health assessment and treatment', TRUE),
    ('11111111-1111-1111-1111-111111111011', 'PULMONOLOGY', 'Pulmonology', 'Respiratory system and lung conditions', TRUE),
    ('11111111-1111-1111-1111-111111111012', 'GASTROENTEROLOGY', 'Gastroenterology', 'Digestive system, stomach, and intestine health', TRUE);

-- 2. HOSPITALS TABLE
CREATE TABLE hospitals (
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    hospital_type VARCHAR(20) NOT NULL,
    registration_identifier VARCHAR(100) UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hospitals_type ON hospitals(hospital_type);
CREATE INDEX idx_hospitals_city ON hospitals(city);
CREATE INDEX idx_hospitals_state ON hospitals(state);
CREATE INDEX idx_hospitals_active ON hospitals(active);
CREATE INDEX idx_hospitals_reg_id ON hospitals(registration_identifier);

-- 3. HEALTHCARE PROVIDERS (DOCTORS) TABLE
CREATE TABLE providers (
    id UUID NOT NULL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    professional_registration_number VARCHAR(100) NOT NULL UNIQUE,
    qualification VARCHAR(200) NOT NULL,
    experience_years INTEGER NOT NULL DEFAULT 0,
    profile_photo_url VARCHAR(500),
    email VARCHAR(150) NOT NULL UNIQUE,
    mobile_number VARCHAR(20) NOT NULL UNIQUE,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_providers_reg_num ON providers(professional_registration_number);
CREATE INDEX idx_providers_email ON providers(email);
CREATE INDEX idx_providers_mobile ON providers(mobile_number);
CREATE INDEX idx_providers_active ON providers(active);

-- 4. PROVIDER SPECIALTIES JOIN TABLE
CREATE TABLE provider_specialties (
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    specialty_id UUID NOT NULL REFERENCES specialties(id) ON DELETE CASCADE,
    PRIMARY KEY (provider_id, specialty_id)
);

CREATE INDEX idx_provider_spec_provider ON provider_specialties(provider_id);
CREATE INDEX idx_provider_spec_specialty ON provider_specialties(specialty_id);

-- 5. PROVIDER HOSPITALS AFFILIATION TABLE
CREATE TABLE provider_hospitals (
    id UUID NOT NULL PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE CASCADE,
    department VARCHAR(100),
    consultation_fee NUMERIC(10,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_provider_hospital UNIQUE (provider_id, hospital_id)
);

CREATE INDEX idx_provider_hosp_provider ON provider_hospitals(provider_id);
CREATE INDEX idx_provider_hosp_hospital ON provider_hospitals(hospital_id);
CREATE INDEX idx_provider_hosp_active ON provider_hospitals(active);
