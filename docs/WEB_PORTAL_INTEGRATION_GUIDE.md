# MediSimbio Web Portals: Developer Integration Guide & Runbook

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\WEB_PORTAL_INTEGRATION_GUIDE.md`  
**Target Audience**: Web Portal Teammates (Doctor, Hospital Admin, Pharmacy Frontends)  
**Date**: October 2026  

---

## 1. Fast-Track Local Setup

### 1.1 Backend Service URLs
- **API Base URL**: `http://localhost:8081/api/v1`
- **Swagger Interactive Documentation**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI 3.1 JSON Spec**: `http://localhost:8081/v3/api-docs`

### 1.2 CORS Allowed Origins
The backend allows cross-origin requests from the following standard web portal dev ports:
- `http://localhost:3000` (Next.js / CRA)
- `http://localhost:5173` (Vite)
- `http://localhost:8080` (Alternative local port)

Preflight `OPTIONS` requests and custom headers (`X-User-Id`, `X-User-Role`, `X-User-Roles`, `Authorization`) are automatically handled and permitted.

---

## 2. Authentication in Development Mode

When developing or testing locally, the Web Portal can bypass an external identity server by sending simple development headers with every request:

### 2.1 Doctor Persona Example:
```http
Authorization: Bearer auth_doc_cardio_1
X-User-Id: auth_doc_cardio_1
X-User-Role: DOCTOR
X-User-Email: dr.gupta@apollo.org
```

### 2.2 Hospital Admin Persona Example:
```http
Authorization: Bearer auth_staff_admin_1
X-User-Id: auth_staff_admin_1
X-User-Role: HOSPITAL_ADMIN
X-User-Email: admin@apollo.org
```

---

## 3. Core Web Portal Workflows & Real API Examples

### 3.1 Doctor Portal: Managing Consultations & Clinical Encounters

#### Step 1: Fetch Doctor Appointments
```bash
curl -X GET "http://localhost:8081/api/v1/providers/d10c0000-0000-0000-0000-000000000001/appointments?date=2026-10-25" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR"
```

#### Step 2: Start Clinical Encounter
```bash
curl -X POST "http://localhost:8081/api/v1/encounters" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR" \
  -d '{
    "appointmentId": "a50c0000-0000-0000-0000-000000000001",
    "encounterType": "IN_PERSON",
    "chiefComplaint": "Severe headaches with visual aura",
    "notes": "Patient seated comfortably in clinic"
  }'
```

Transition to `IN_PROGRESS`:
```bash
curl -X POST "http://localhost:8081/api/v1/encounters/c70c0000-0000-0000-0000-000000000001/start" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR"
```

#### Step 3: Record Vitals
```bash
curl -X POST "http://localhost:8081/api/v1/encounters/c70c0000-0000-0000-0000-000000000001/vitals" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR" \
  -d '{
    "systolicBp": 120,
    "diastolicBp": 80,
    "heartRate": 72,
    "temperatureCelsius": 37.0,
    "spo2Percentage": 99,
    "clinicalNotes": "Vitals within normal limits"
  }'
```

#### Step 4: Record ICD-10 Diagnoses
```bash
curl -X POST "http://localhost:8081/api/v1/encounters/c70c0000-0000-0000-0000-000000000001/diagnoses" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR" \
  -d '{
    "icdCode": "G43.909",
    "diagnosisDescription": "Migraine, unspecified, not intractable, without status migrainosus",
    "category": "NEUROLOGICAL",
    "clinicalStatus": "CONFIRMED",
    "verificationStatus": "PRIMARY"
  }'
```

#### Step 5: Author and Finalize E-Prescription
```bash
curl -X POST "http://localhost:8081/api/v1/encounters/c70c0000-0000-0000-0000-000000000001/prescriptions" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR" \
  -d '{
    "finalizeImmediately": true,
    "validUntil": "2026-11-25T00:00:00Z",
    "clinicalInstructions": "Take Sumatriptan at onset of migraine headache. Drink plenty of fluids.",
    "items": [
      {
        "medicineName": "Sumatriptan 50mg",
        "activeIngredient": "Sumatriptan Succinate",
        "dose": "1 tablet",
        "route": "ORAL",
        "frequency": "PRN (As needed)",
        "durationValue": 30,
        "durationUnit": "DAYS",
        "quantity": 6,
        "quantityUnit": "TABLETS",
        "refillsAllowed": 1,
        "instructions": "Max 2 tablets in 24 hours"
      }
    ]
  }'
```

#### Step 6: Complete Encounter
```bash
curl -X POST "http://localhost:8081/api/v1/encounters/c70c0000-0000-0000-0000-000000000001/complete" \
  -H "X-User-Id: auth_doc_cardio_1" \
  -H "X-User-Role: DOCTOR"
```

---

### 3.2 Hospital Admin Portal: Scheduling & Facility Operations

#### 1. Configure Doctor Weekly Availability
```bash
curl -X POST "http://localhost:8081/api/v1/providers/d10c0000-0000-0000-0000-000000000001/availability" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: auth_staff_admin_1" \
  -H "X-User-Role: HOSPITAL_ADMIN" \
  -d '{
    "hospitalId": "h20c0000-0000-0000-0000-000000000001",
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "13:00:00",
    "slotDurationMinutes": 30,
    "maxBookingsPerSlot": 1
  }'
```

#### 2. Query Facility Appointments Roster
```bash
curl -X GET "http://localhost:8081/api/v1/hospitals/h20c0000-0000-0000-0000-000000000001/appointments?date=2026-10-25" \
  -H "X-User-Id: auth_staff_admin_1" \
  -H "X-User-Role: HOSPITAL_ADMIN"
```
