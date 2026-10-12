# MediSimbio — Phase 6 Implementation Plan: E-Prescription & Medicine Dispensing Orders

## 1. Objectives & Scope
Implement Phase 6 of the MediSimbio healthcare ecosystem backend:
- Authoritative E-Prescription domain (`PrescriptionEntity`, `PrescriptionItemEntity`, lifecycle state machine).
- Provider authorization (only authorized attending provider can author/finalize/cancel/replace).
- Patient access control (read-only access to own prescriptions).
- Hospital access control (hospital staff read access within authorized hospital scope; cannot author).
- Immutability of finalized prescriptions & concurrency-safe replacement workflow (`SUPERSEDED`).
- Medicine Dispensing Order subsystem (`DispensingOrderEntity`, `DispensingOrderItemEntity`, fulfillment lifecycle).
- Medical timeline integration (including finalized prescriptions with zero N+1 and database-level pagination).
- Flyway V8 schema migration (`common` directory for PostgreSQL 16 and H2 parity).
- Robust integration and unit tests covering all lifecycle, IDOR, authorization, and concurrency cases.

---

## 2. Phased Implementation Tasks

### Phase 6.1: Database Schema Migration (Flyway V8)
- File: `src/main/resources/db/migration/common/V8__create_prescription_and_dispensing_schema.sql`
- Tables:
  1. `prescriptions`: UUID PK, human-readable unique reference (`RX-YYYYMMDD-XXXXXX`), patient_id, encounter_id, provider_id, hospital_id, status (DRAFT, FINALIZED, CANCELLED, SUPERSEDED), prescribed_at, finalized_at, valid_until, clinical_instructions, version_number, replaces_prescription_id (unique, self-referencing), cancellation_reason, cancelled_at, cancelled_by, audit timestamps.
  2. `prescription_items`: UUID PK, prescription_id FK, medicine_name, active_ingredient, strength, dosage_form, dose, route, frequency, duration_value, duration_unit, quantity, quantity_unit, refills_allowed, instructions, sequence_number.
  3. `dispensing_orders`: UUID PK, human-readable unique reference (`DSP-YYYYMMDD-XXXXXX`), prescription_id FK, patient_id FK, hospital_id FK, status (PENDING, ACCEPTED, PARTIALLY_DISPENSED, DISPENSED, CANCELLED, REJECTED), ordered_at, notes, audit timestamps.
  4. `dispensing_order_items`: UUID PK, dispensing_order_id FK, prescription_item_id FK, medicine_name, requested_quantity.
  5. Foreign keys with `ON DELETE RESTRICT` on clinical parents; composite and lookup indexes.

### Phase 6.2: Prescription Module Implementation
- Packages: `com.medisimbio.backend.prescription.*`
- Enums: `PrescriptionStatus` (`DRAFT`, `FINALIZED`, `CANCELLED`, `SUPERSEDED`)
- Entities: `PrescriptionEntity`, `PrescriptionItemEntity`
- DTOs: `CreatePrescriptionRequestDto`, `UpdatePrescriptionRequestDto`, `PrescriptionItemRequestDto`, `PrescriptionResponseDto`, `PrescriptionSummaryDto`, `PrescriptionItemResponseDto`, `CancelPrescriptionRequestDto`, `ReplacePrescriptionRequestDto`
- Mapper: `PrescriptionMapper`
- Repositories: `PrescriptionRepository`, `PrescriptionItemRepository`
- Service & Implementation: `PrescriptionService`, `PrescriptionServiceImpl`
  - Validations: Encounter eligibility, provider explicit auth check, patient match, draft mutability, finalization constraints, cancel reason, atomic replacement with optimistic/pessimistic lock.
- Controller: `PrescriptionController` (`/api/v1/...`)

### Phase 6.3: Dispensing Order Module Implementation
- Packages: `com.medisimbio.backend.dispensing.*`
- Enums: `DispensingOrderStatus` (`PENDING`, `ACCEPTED`, `PARTIALLY_DISPENSED`, `DISPENSED`, `CANCELLED`, `REJECTED`)
- Entities: `DispensingOrderEntity`, `DispensingOrderItemEntity`
- DTOs: `CreateDispensingOrderRequestDto`, `DispensingOrderResponseDto`, `DispensingOrderItemResponseDto`
- Mapper: `DispensingOrderMapper`
- Repositories: `DispensingOrderRepository`, `DispensingOrderItemRepository`
- Service & Implementation: `DispensingOrderService`, `DispensingOrderServiceImpl`
  - Validations: Prescription must be FINALIZED, patient matching, active order conflict check (prevent duplicates), safe transition policy.
- Controller: `DispensingOrderController` (`/api/v1/...`)

### Phase 6.4: Medical Timeline Integration
- Update `MedicalTimelineRepository.java`:
  - Add 4th UNION ALL branch for `PRESCRIPTION` (filtering `status IN ('FINALIZED', 'SUPERSEDED')`).
  - Update `countPatientTimelineEvents` to include finalized prescriptions count.
- Verify `MedicalRecordServiceImpl.java` projection mapping.

### Phase 6.5: Automated Testing & Verification
- Unit & integration tests:
  - `PrescriptionControllerTest.java`
  - `DispensingOrderControllerTest.java`
  - `PrescriptionLifecycleTest.java` (Draft -> Finalized -> Superseded / Cancelled, concurrency protection)
  - Updated `MedicalTimelinePaginationTest.java` (verify prescriptions appear on timeline)
- Run full test suite: `./gradlew clean test` and `./gradlew build`.

### Phase 6.6: Documentation & Reporting
- Update `docs/PHASE_6_STATUS.md` and `README.md`.
- Produce final report.
