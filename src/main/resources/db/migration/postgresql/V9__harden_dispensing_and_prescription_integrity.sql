-- =============================================================================
-- MediSimbio Database Migration: V9 Harden Dispensing & Prescription Integrity (PostgreSQL)
-- Description:
--   1. Protect prescription items from accidental cascade deletion via ON DELETE RESTRICT.
--   2. Add persisted dispensed_quantity and range constraint to dispensing_order_items.
--   3. Database-authoritative partial unique index on active dispensing orders.
-- =============================================================================

-- 1. Protect Prescription Items from Cascade Deletion
ALTER TABLE prescription_items DROP CONSTRAINT IF EXISTS prescription_items_prescription_id_fkey;
ALTER TABLE prescription_items ADD CONSTRAINT fk_prescription_items_prescription
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE RESTRICT;

-- 2. Persisted Dispensed Quantity with Range Invariant on Dispensing Order Items
ALTER TABLE dispensing_order_items ADD COLUMN dispensed_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE dispensing_order_items ADD CONSTRAINT chk_dispensed_qty_bounds
    CHECK (dispensed_quantity >= 0 AND dispensed_quantity <= requested_quantity);

-- 3. Database-Authoritative Partial Unique Index on Active Dispensing Orders
-- Enforces at most one active dispensing order (PENDING, ACCEPTED, PARTIALLY_DISPENSED)
-- per prescription at the PostgreSQL storage engine level.
CREATE UNIQUE INDEX uq_dispensing_orders_active_prescription
ON dispensing_orders (prescription_id)
WHERE status IN ('PENDING', 'ACCEPTED', 'PARTIALLY_DISPENSED');
