-- =============================================================================
-- MediSimbio Database Migration: V7 Harden Identity & Clinical Integrity
-- Description:
--   1. Add auth_user_id column to providers table for explicit identity mapping.
--   2. Create hospital_staff table for multi-user hospital staff authorization.
--   3. Restrict foreign keys on diagnoses and vitals to prevent accidental deletion.
-- =============================================================================

-- 1. Explicit Identity Mapping for Healthcare Providers
ALTER TABLE providers ADD COLUMN auth_user_id VARCHAR(128) UNIQUE;
CREATE INDEX idx_providers_auth_user_id ON providers(auth_user_id);

-- 2. Multi-user Hospital Staff Membership Table
CREATE TABLE hospital_staff (
    id UUID NOT NULL PRIMARY KEY,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE CASCADE,
    auth_user_id VARCHAR(128) NOT NULL,
    staff_name VARCHAR(150),
    role VARCHAR(50) NOT NULL DEFAULT 'STAFF',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_hospital_staff UNIQUE (hospital_id, auth_user_id)
);

CREATE INDEX idx_hospital_staff_auth ON hospital_staff(auth_user_id, active);
CREATE INDEX idx_hospital_staff_hosp ON hospital_staff(hospital_id, active);

-- 3. Protect Clinical History by Restricting Foreign Key Cascades
ALTER TABLE diagnoses DROP CONSTRAINT IF EXISTS diagnoses_encounter_id_fkey;
ALTER TABLE diagnoses ADD CONSTRAINT fk_diagnoses_encounter
    FOREIGN KEY (encounter_id) REFERENCES encounters(id) ON DELETE RESTRICT;

ALTER TABLE vitals DROP CONSTRAINT IF EXISTS vitals_encounter_id_fkey;
ALTER TABLE vitals ADD CONSTRAINT fk_vitals_encounter
    FOREIGN KEY (encounter_id) REFERENCES encounters(id) ON DELETE RESTRICT;
