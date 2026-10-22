# MediSimbio Backend: Security & Permission Matrix

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\SECURITY_AND_PERMISSION_MATRIX.md`  
**Date**: October 2026  

---

## 1. Role Definitions

| Role Identifier | Description | Target Persona |
|---|---|---|
| `ROLE_PATIENT` | Healthcare consumer / patient | Mobile app & patient portal users |
| `ROLE_DOCTOR` | Licensed healthcare provider | Doctor portal & consulting clinicians |
| `ROLE_HOSPITAL_ADMIN` | Hospital administrative authority | Hospital management & staff |
| `ROLE_PHARMACY` | Dispensing pharmacist | Pharmacy fulfillment portal |
| `ROLE_SYSTEM_ADMIN` | Platform system administrator | Platform operations & catalog curators |

---

## 2. Resource-Level Access & IDOR Protection Matrix

In addition to coarse role checks, MediSimbio enforces **resource-level ownership checks** across all clinical data:

| Entity / Domain | Endpoint Group | Permitted Roles | Resource-Level Enforcement Rule |
|---|---|---|---|
| **Patient Profile** | `/api/v1/patients/me/**` | `ROLE_PATIENT` | Resolved directly from security context `authUserId`. No client ID accepted. |
| **National Med-ID** | `/api/v1/patients/me/med-id` | `ROLE_PATIENT` | Tied directly to the authenticated patient record. |
| **Appointments** | `/api/v1/appointments/me` | `ROLE_PATIENT` | Queries appointments where `patient.authUserId = currentUserId`. |
| **Appointment Details** | `/api/v1/appointments/{id}` | Patient / Doctor / Staff | User must be the booked patient, assigned provider, or active hospital staff. |
| **Clinical Encounters** | `/api/v1/encounters/{id}` | Patient / Doctor / Staff | User must be the encounter patient, assigned provider, or active hospital staff. |
| **Encounter Actions** | `.../{id}/start`, `complete`, `cancel` | Attending Doctor | User's `authUserId` must match `encounter.provider.authUserId`. |
| **Vitals Entry** | `/api/v1/encounters/{id}/vitals` | Doctor / Nurse | Permitted only during active encounters (`IN_PROGRESS`). |
| **Diagnoses Entry** | `/api/v1/encounters/{id}/diagnoses` | Attending Doctor | Permitted only while encounter is open (`IN_PROGRESS`). |
| **Prescriptions Authoring** | `/api/v1/encounters/{id}/prescriptions` | Attending Doctor | Provider must author prescription during valid encounter. |
| **Prescription Access** | `/api/v1/prescriptions/{id}` | Patient / Doctor / Staff | **Patients cannot view DRAFT prescriptions**. Finalized only. |
| **Dispensing Orders** | `/api/v1/prescriptions/{id}/dispensing-orders` | `ROLE_PATIENT` | Only the prescription's owning patient may place dispensing orders. |
| **Dispensing Cancellation** | `.../dispensing-orders/{id}/cancel` | `ROLE_PATIENT` | Permitted only by the owning patient and only while status is `PENDING`. |
| **Medical Timeline** | `/api/v1/patients/me/medical-record/timeline` | `ROLE_PATIENT` | Queries native 4-way UNION ALL filtered strictly by `patient.authUserId`. |
| **AI Symptom Triage** | `/api/v1/ai/triage` | Patient / Doctor | Analyzes symptom payload and suggests appropriate specialty. |
| **AI Prescription Safety** | `/api/v1/ai/prescriptions/safety-check` | Doctor / Staff / Patient | Screens medications against allergies and concurrent drug interactions. |

---

## 3. Detailed Endpoint-to-Permission Matrix

| API Path | Method | Public | Patient | Doctor | Hospital Admin | Pharmacy | System Admin |
|---|---|---|---|---|---|---|---|
| `/api/v1/health/**` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/med-id/verify/{id}` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/specialties` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/specialties` | POST | NO | NO | NO | **YES** | NO | **YES** |
| `/api/v1/hospitals` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/hospitals` | POST | NO | NO | NO | **YES** | NO | **YES** |
| `/api/v1/providers` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/providers` | POST | NO | NO | NO | NO | NO | **YES** |
| `/api/v1/providers/{id}/availability` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/providers/{id}/availability` | POST | NO | NO | **YES** | **YES** | NO | NO |
| `/api/v1/providers/{id}/slots` | GET | **YES** | YES | YES | YES | YES | YES |
| `/api/v1/appointments` | POST | NO | **YES** | NO | NO | NO | NO |
| `/api/v1/appointments/me` | GET | NO | **YES** | NO | NO | NO | NO |
| `/api/v1/appointments/{id}` | GET | NO | **YES** (Own) | **YES** (Own) | **YES** (Facility) | NO | NO |
| `/api/v1/encounters` | POST | NO | NO | **YES** | **YES** | NO | NO |
| `/api/v1/encounters/{id}/start` | POST | NO | NO | **YES** (Attending) | NO | NO | NO |
| `/api/v1/encounters/{id}/complete` | POST | NO | NO | **YES** (Attending) | NO | NO | NO |
| `/api/v1/encounters/{id}/vitals` | POST | NO | NO | **YES** | **YES** | NO | NO |
| `/api/v1/encounters/{id}/diagnoses` | POST | NO | NO | **YES** (Attending) | NO | NO | NO |
| `/api/v1/encounters/{id}/prescriptions` | POST | NO | NO | **YES** (Attending) | NO | NO | NO |
| `/api/v1/prescriptions/{id}` | GET | NO | **YES** (Finalized) | **YES** (Own) | **YES** (Facility) | **YES** | NO |
| `/api/v1/prescriptions/{id}/finalize` | POST | NO | NO | **YES** (Author) | NO | NO | NO |
| `/api/v1/prescriptions/{id}/dispensing-orders` | POST | NO | **YES** (Own) | NO | NO | NO | NO |
| `/api/v1/dispensing-orders/{id}` | GET | NO | **YES** (Own) | **YES** | **YES** | **YES** | NO |
| `/api/v1/patients/me/medical-record/timeline` | GET | NO | **YES** (Own) | NO | NO | NO | NO |
| `/api/v1/ai/triage` | POST | NO | **YES** | **YES** | NO | NO | NO |
| `/api/v1/ai/prescriptions/safety-check` | POST | NO | **YES** | **YES** | **YES** | NO | NO |
