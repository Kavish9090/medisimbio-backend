# MediSimbio Backend: Phase 5 Analysis & Architecture Design
**Subsystem**: Clinical Encounters, Diagnoses, Vitals, and Patient Medical Record Timeline  
**Date**: 2026-10-06  
**Status**: IN PROGRESS (Phase 5)

---

## 1. Context & Business Domain

Following the completion of Patient Management (Phase 2), Provider/Hospital Catalog (Phase 3), and Consultation Appointment Booking (Phase 4), Phase 5 implements the authoritative **Clinical Documentation Core**.

```
PATIENT
   ↓
APPOINTMENT (SCHEDULED / CONFIRMED)
   ↓
ENCOUNTER (CONSULTATION / FOLLOW_UP / EMERGENCY / INPATIENT / OUTPATIENT) [OPEN]
   ↓
├── VITALS RECORDING (Temperature, BP, Pulse, SpO2, BMI)
├── CLINICAL NOTES & CHIEF COMPLAINT
├── DIAGNOSES (PRIMARY / SECONDARY / DIFFERENTIAL)
└── TREATMENT & FOLLOW-UP SUMMARY
   ↓
ENCOUNTER COMPLETION [COMPLETED] (Transactionally marks Appointment -> COMPLETED)
   ↓
PATIENT MEDICAL RECORD & CHRONOLOGICAL TIMELINE (/api/v1/patients/me/medical-records/timeline)
```

---

## 2. Module Boundaries & Responsibilities

### 2.1 Encounter Module (`com.medisimbio.backend.encounter`)
- **Entity**: `EncounterEntity` mapped to `encounters` table.
- **Reference Generator**: `EncounterReferenceGenerator` generating format `ENC-YYYYMMDD-XXXX`.
- **Encounter Types**: `CONSULTATION`, `FOLLOW_UP`, `EMERGENCY`, `INPATIENT`, `OUTPATIENT`.
- **Encounter Status**: `OPEN`, `COMPLETED`, `CANCELLED`.
- **Lifecycle Enforcer**:
  - `OPEN` $\rightarrow$ `COMPLETED` (Sets `endedAt`, transactionally completes linked appointment if present).
  - `OPEN` $\rightarrow$ `CANCELLED` (Sets `endedAt`).
  - Prohibits transitions out of terminal states (`COMPLETED` or `CANCELLED`) with `409 Conflict`.
- **Appointment Correlation**: Validates patient, provider, and hospital match when initialized from an appointment. Prevents multiple active encounters for the same appointment.
- **Access Control & Directory**:
  - Patient self-service (`/api/v1/patients/me/encounters`) resolved via `AuthenticationFacade`.
  - Provider directory (`/api/v1/providers/{providerId}/encounters`).
  - Hospital directory (`/api/v1/hospitals/{hospitalId}/encounters`).

### 2.2 Diagnosis Module (`com.medisimbio.backend.diagnosis`)
- **Entity**: `DiagnosisEntity` mapped to `diagnoses` table.
- **Diagnosis Types**: `PRIMARY`, `SECONDARY`, `DIFFERENTIAL`.
- **Clinical Association**: Linked directly to an `EncounterEntity`. Can add multiple diagnoses to an open encounter.

### 2.3 Vitals Subsystem (`com.medisimbio.backend.vitals`)
- **Entity**: `EncounterVitalsEntity` mapped to `encounter_vitals` table.
- **Numeric Fields & Validation**:
  - `temperature`: 70.0 - 115.0 °F (numeric)
  - `heartRate`: 30 - 250 bpm (integer)
  - `respiratoryRate`: 5 - 60 breaths/min (integer)
  - `systolicBloodPressure`: 50 - 300 mmHg (integer)
  - `diastolicBloodPressure`: 30 - 200 mmHg (integer)
  - `oxygenSaturation`: 50.0 - 100.0 % (numeric)
  - `weightKg`: 0.5 - 500.0 kg (numeric)
  - `heightCm`: 20.0 - 300.0 cm (numeric)
  - `bmi`: Auto-calculated $kg / (m^2)$ if not explicitly provided.

### 2.4 Medical Record & Timeline Subsystem (`com.medisimbio.backend.medicalrecord`)
- **Service**: `MedicalRecordService` aggregating patient's past encounters, diagnoses, vitals, and consultation history into a unified chronological medical timeline.
- **Projections**: Read-optimized timeline DTOs without leaking unneeded data.

---

## 3. Database Schema (Flyway Migration `V5`)

```sql
-- encounters
CREATE TABLE encounters (
    id UUID PRIMARY KEY,
    encounter_reference VARCHAR(30) NOT NULL UNIQUE,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES providers(id) ON DELETE RESTRICT,
    hospital_id UUID REFERENCES hospitals(id) ON DELETE RESTRICT,
    appointment_id UUID REFERENCES appointments(id) ON DELETE SET NULL,
    encounter_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    chief_complaint VARCHAR(1000),
    clinical_notes TEXT,
    diagnosis_summary VARCHAR(1000),
    treatment_summary VARCHAR(1000),
    follow_up_notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- diagnoses
CREATE TABLE diagnoses (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL REFERENCES encounters(id) ON DELETE RESTRICT,
    diagnosis_code VARCHAR(50),
    diagnosis_name VARCHAR(255) NOT NULL,
    diagnosis_type VARCHAR(30) NOT NULL DEFAULT 'PRIMARY',
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- encounter_vitals
CREATE TABLE encounter_vitals (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL REFERENCES encounters(id) ON DELETE RESTRICT,
    temperature NUMERIC(4,1),
    heart_rate INTEGER,
    respiratory_rate INTEGER,
    systolic_blood_pressure INTEGER,
    diastolic_blood_pressure INTEGER,
    oxygen_saturation NUMERIC(4,1),
    weight_kg NUMERIC(5,2),
    height_cm NUMERIC(5,2),
    bmi NUMERIC(4,1),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. API Endpoints Map

| Subsystem | Method | Endpoint | Access |
|---|---|---|---|
| **Encounter** | `POST` | `/api/v1/encounters` | Authenticated Clinician |
| **Encounter** | `GET` | `/api/v1/encounters/{id}` | Authenticated Clinician / Patient (Owner) |
| **Encounter** | `PATCH` | `/api/v1/encounters/{id}` | Authenticated Clinician |
| **Encounter** | `POST` | `/api/v1/encounters/{id}/complete` | Authenticated Clinician |
| **Encounter** | `POST` | `/api/v1/encounters/{id}/cancel` | Authenticated Clinician |
| **Encounter** | `GET` | `/api/v1/patients/me/encounters` | Authenticated Patient |
| **Encounter** | `GET` | `/api/v1/providers/{providerId}/encounters` | Authenticated Clinician |
| **Encounter** | `GET` | `/api/v1/hospitals/{hospitalId}/encounters` | Authenticated Hospital Admin/Staff |
| **Diagnosis** | `POST` | `/api/v1/encounters/{encounterId}/diagnoses` | Authenticated Clinician |
| **Diagnosis** | `GET` | `/api/v1/encounters/{encounterId}/diagnoses` | Authenticated Clinician / Patient |
| **Diagnosis** | `PATCH` | `/api/v1/diagnoses/{id}` | Authenticated Clinician |
| **Diagnosis** | `DELETE`| `/api/v1/diagnoses/{id}` | Authenticated Clinician |
| **Vitals** | `POST` | `/api/v1/encounters/{encounterId}/vitals` | Authenticated Clinician |
| **Vitals** | `GET` | `/api/v1/encounters/{encounterId}/vitals` | Authenticated Clinician / Patient |
| **Medical Record**| `GET` | `/api/v1/patients/me/medical-records` | Authenticated Patient |
| **Medical Record**| `GET` | `/api/v1/patients/me/medical-records/timeline` | Authenticated Patient |
