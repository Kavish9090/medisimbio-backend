# MediSimbio AI Inference Service: Integration Contract & Clinical Safety Specification

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\AI_INTEGRATION_CONTRACT.md`  
**Target Audience**: AI Teammate (Python / FastAPI / Triton / vLLM Engineer) & Clinical Safety Architects  
**Date**: October 2026  

---

## 1. System Architecture & Integration Model

The MediSimbio backend acts as the authoritative orchestration and clinical governance layer. The external AI model service acts as an **advisory intelligence engine**:

```
+--------------------------+       HTTP / REST       +-----------------------------+
|    MediSimbio Backend    | ----------------------> | AI Model / Inference Server |
|   (Java 21 / Spring)     | <---------------------- |   (FastAPI / PyTorch / LLM) |
|   AiIntegrationService   |     Strict Typed DTOs   |   Port: 8000 (Default)      |
+--------------------------+                         +-----------------------------+
```

### 1.1 Backend Configuration Properties
In `application.yml`:
```yaml
app:
  ai:
    enabled: ${AI_SERVICE_ENABLED:true}
    service-url: ${AI_SERVICE_URL:http://localhost:8000}
    connect-timeout-ms: 3000
    read-timeout-ms: 8000
```

---

## 2. Integration Contract 1: Pre-Consultation Symptom Triage

### 2.1 Endpoint
`POST /api/v1/ai/triage` (Forwarded to AI Service: `POST /infer/triage`)

### 2.2 Input Payload (`AiTriageRequestDto`)
```json
{
  "patientAge": 45,
  "gender": "MALE",
  "symptoms": [
    "Severe substernal chest pressure",
    "Shortness of breath on exertion",
    "Diaphoresis"
  ],
  "description": "Pressure started 45 minutes ago while climbing stairs. Radiating to left shoulder.",
  "duration": "45 minutes",
  "severityScale": 8
}
```

### 2.3 Field Specification:
| Field | Type | Required | Description |
|---|---|---|---|
| `patientAge` | Integer | No | Patient age in years |
| `gender` | String | No | Biological sex (`MALE`, `FEMALE`, `OTHER`) |
| `symptoms` | List<String> | **Yes** | Standardized or reported symptom tokens |
| `description` | String | **Yes** | Patient free-text symptom description |
| `duration` | String | No | Temporal onset / duration of symptoms |
| `severityScale` | Integer | No | Patient subjective scale (1 to 10) |

### 2.4 Output Payload (`AiTriageResponseDto`)
```json
{
  "triageLevel": "EMERGENCY",
  "recommendedSpecialtyCode": "CARD",
  "recommendedSpecialtyName": "Cardiology",
  "clinicalIntakeSummary": "45-year-old male presenting with acute chest pressure radiating to left shoulder and diaphoresis. Immediate clinical triage required.",
  "differentialObservations": [
    "Acute Coronary Syndrome (ACS)",
    "Acute Myocardial Infarction",
    "Aortic Dissection"
  ],
  "redFlagWarnings": [
    "Immediate emergency room triage advised if chest pain, syncope, or severe dyspnea occurs"
  ],
  "confidenceScore": 0.94
}
```

### 2.5 Permitted `triageLevel` Values:
- `EMERGENCY`: Immediate life-threatening signs; direct to Emergency Department.
- `URGENT`: Requires same-day physician consultation.
- `ROUTINE`: Standard clinic booking.
- `SELF_CARE`: Minor self-limiting symptoms with advisory guidance.

---

## 3. Integration Contract 2: Prescription Safety Screening

### 3.1 Endpoint
`POST /api/v1/ai/prescriptions/safety-check` (Forwarded to AI Service: `POST /infer/prescription-safety`)

### 3.2 Input Payload (`AiPrescriptionSafetyCheckRequestDto`)
```json
{
  "patientId": "e30c0000-0000-0000-0000-000000000001",
  "candidateMedicines": [
    "Amoxicillin 500mg"
  ],
  "currentMedications": [
    "Warfarin 5mg"
  ],
  "knownAllergies": [
    "Penicillin"
  ],
  "activeConditions": [
    "Hypertension",
    "Atrial Fibrillation"
  ]
}
```

### 3.3 Output Payload (`AiPrescriptionSafetyCheckResponseDto`)
```json
{
  "safetyLevel": "CONTRAINDICATED",
  "allergyAlerts": [
    "Potential allergen match: 'Amoxicillin 500mg' matches documented allergy 'Penicillin'"
  ],
  "interactionWarnings": [
    "Potential INR elevation and bleeding risk when combining broad-spectrum antibiotics with Warfarin"
  ],
  "dosageRecommendations": [
    "Select non-beta-lactam alternative (e.g. Macrolide or Fluoroquinolone based on culture)"
  ],
  "summary": "Candidate prescription triggered 1 critical allergen alert(s). Review required before issuance."
}
```

---

## 4. Clinical Safety Guarantees & Privacy Rules

1. **Advisory Role Only**: AI predictions are strictly advisory. Final clinical authorization and prescription signing is reserved exclusively to the licensed human doctor (`ROLE_DOCTOR`).
2. **Zero Autonomous Prescribing**: The backend rejects any automated prescription creation without human provider identity in the security context.
3. **Resilient Fallback Mode**: If the external inference server is offline, times out, or returns 5xx:
   - The backend catches the exception and logs an operational alert.
   - Responds with safe deterministic fallback rules based on patient severity score.
   - Includes an explicit note that the response is a fallback and live inference was unavailable.
4. **Data Minimization & Privacy**: The backend strips all patient PII (name, phone, national ID, address) before invoking the AI service. Only age, biological sex, symptom descriptions, and drug names are transmitted.
