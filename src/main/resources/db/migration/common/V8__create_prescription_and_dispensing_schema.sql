-- =============================================================================
-- MediSimbio Database Migration: V8 Create E-Prescription & Dispensing Orders Schema
-- Description: Creates relational tables for Prescriptions, Prescription Items,
--              Medicine Dispensing Orders, and Dispensing Order Items with
--              auditing, foreign keys, and indexes for clinical tracking.
-- =============================================================================

-- 1. PRESCRIPTIONS TABLE
CREATE TABLE prescriptions (
    id UUID NOT NULL PRIMARY KEY,
    prescription_reference VARCHAR(40) NOT NULL UNIQUE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    encounter_id UUID NOT NULL REFERENCES encounters(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE RESTRICT,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    prescribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_at TIMESTAMP,
    valid_until DATE,
    clinical_instructions TEXT,
    version_number INTEGER NOT NULL DEFAULT 1,
    replaces_prescription_id UUID REFERENCES prescriptions(id) ON DELETE RESTRICT,
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    cancelled_by VARCHAR(128),
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_prescriptions_replaces UNIQUE (replaces_prescription_id)
);

CREATE INDEX idx_prescriptions_patient ON prescriptions(patient_id, prescribed_at);
CREATE INDEX idx_prescriptions_encounter ON prescriptions(encounter_id);
CREATE INDEX idx_prescriptions_provider ON prescriptions(provider_id, prescribed_at);
CREATE INDEX idx_prescriptions_hospital ON prescriptions(hospital_id);
CREATE INDEX idx_prescriptions_status ON prescriptions(status);
CREATE INDEX idx_prescriptions_ref ON prescriptions(prescription_reference);
CREATE INDEX idx_prescriptions_replaces ON prescriptions(replaces_prescription_id);

-- 2. PRESCRIPTION ITEMS TABLE
CREATE TABLE prescription_items (
    id UUID NOT NULL PRIMARY KEY,
    prescription_id UUID NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_name VARCHAR(255) NOT NULL,
    active_ingredient VARCHAR(255),
    strength VARCHAR(100),
    dosage_form VARCHAR(50),
    dose VARCHAR(100) NOT NULL,
    route VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration_value INTEGER,
    duration_unit VARCHAR(20),
    quantity INTEGER NOT NULL,
    quantity_unit VARCHAR(50),
    refills_allowed INTEGER NOT NULL DEFAULT 0,
    instructions VARCHAR(500),
    sequence_number INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prescription_items_prescription ON prescription_items(prescription_id);

-- 3. MEDICINE DISPENSING ORDERS TABLE
CREATE TABLE dispensing_orders (
    id UUID NOT NULL PRIMARY KEY,
    dispensing_reference VARCHAR(40) NOT NULL UNIQUE,
    prescription_id UUID NOT NULL REFERENCES prescriptions(id) ON DELETE RESTRICT,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    hospital_id UUID NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dispensing_orders_prescription ON dispensing_orders(prescription_id);
CREATE INDEX idx_dispensing_orders_patient ON dispensing_orders(patient_id, ordered_at);
CREATE INDEX idx_dispensing_orders_status ON dispensing_orders(status);
CREATE INDEX idx_dispensing_orders_ref ON dispensing_orders(dispensing_reference);

-- 4. DISPENSING ORDER ITEMS TABLE
CREATE TABLE dispensing_order_items (
    id UUID NOT NULL PRIMARY KEY,
    dispensing_order_id UUID NOT NULL REFERENCES dispensing_orders(id) ON DELETE CASCADE,
    prescription_item_id UUID NOT NULL REFERENCES prescription_items(id) ON DELETE RESTRICT,
    medicine_name VARCHAR(255) NOT NULL,
    requested_quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dispensing_order_items_order ON dispensing_order_items(dispensing_order_id);
