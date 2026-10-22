# MediSimbio Backend: Complete API Endpoint Inventory

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\API_ENDPOINT_INVENTORY.md`  
**Base URL**: `http://localhost:8081`  
**API Prefix**: `/api/v1`  
**Swagger UI**: `http://localhost:8081/swagger-ui.html`  
**OpenAPI Specification**: `http://localhost:8081/v3/api-docs`  

---

## 1. System Health & Metadata Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `GET` | `/api/v1/health` | No | Public | None | `ApiResponse<HealthResponseDto>` | Verified (100% Pass) |
| `GET` | `/actuator/health` | No | Public | None | Actuator Health JSON | Verified (100% Pass) |
| `GET` | `/actuator/info` | No | Public | None | Actuator Info JSON | Verified (100% Pass) |
| `GET` | `/v3/api-docs` | No | Public | None | OpenAPI 3.1 Spec | Verified (100% Pass) |
| `GET` | `/swagger-ui.html` | No | Public | None | Swagger UI Console | Verified (100% Pass) |

---

## 2. Patient & National Med-ID Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `POST` | `/api/v1/patients` | Yes | Any Authenticated | `CreatePatientRequestDto` | `ApiResponse<PatientResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/patients/me` | Yes | `ROLE_PATIENT` | None | `ApiResponse<PatientResponseDto>` | Verified (100% Pass) |
| `PATCH` | `/api/v1/patients/me` | Yes | `ROLE_PATIENT` | `UpdatePatientRequestDto` | `ApiResponse<PatientResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/patients/me/med-id` | Yes | `ROLE_PATIENT` | None | `ApiResponse<MedIdResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/patients/me/med-id` | Yes | `ROLE_PATIENT` | None | `ApiResponse<MedIdResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/med-id/verify/{medId}` | No | Public | None | `ApiResponse<MedIdVerificationDto>` | Verified (100% Pass) |

---

## 3. Hospital, Provider & Specialty Catalog Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `GET` | `/api/v1/specialties` | No | Public | None | `ApiResponse<List<SpecialtyResponseDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/specialties/{id}` | No | Public | None | `ApiResponse<SpecialtyResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/specialties/code/{code}` | No | Public | None | `ApiResponse<SpecialtyResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/specialties` | Yes | `ROLE_HOSPITAL_ADMIN`, `ROLE_SYSTEM_ADMIN` | `CreateSpecialtyRequestDto` | `ApiResponse<SpecialtyResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/hospitals` | No | Public | None | `ApiResponse<PageResponse<HospitalResponseDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/hospitals/{id}` | No | Public | None | `ApiResponse<HospitalResponseDto>` | Verified (100% Pass) |
| `PATCH` | `/api/v1/hospitals/{id}` | Yes | `ROLE_HOSPITAL_ADMIN` | `UpdateHospitalRequestDto` | `ApiResponse<HospitalResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/hospitals/{id}/providers` | No | Public | None | `ApiResponse<PageResponse<ProviderSummaryDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/hospitals` | Yes | `ROLE_HOSPITAL_ADMIN`, `ROLE_SYSTEM_ADMIN` | `CreateHospitalRequestDto` | `ApiResponse<HospitalResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers` | No | Public | None | `ApiResponse<PageResponse<ProviderSummaryDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{id}` | No | Public | None | `ApiResponse<ProviderResponseDto>` | Verified (100% Pass) |
| `PATCH` | `/api/v1/providers/{id}` | Yes | `ROLE_DOCTOR`, `ROLE_SYSTEM_ADMIN` | `UpdateProviderRequestDto` | `ApiResponse<ProviderResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/providers` | Yes | `ROLE_SYSTEM_ADMIN` | `CreateProviderRequestDto` | `ApiResponse<ProviderResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{id}/specialties` | No | Public | None | `ApiResponse<List<SpecialtyResponseDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/providers/{id}/specialties` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | `AssignSpecialtyRequestDto` | `ApiResponse<Void>` | Verified (100% Pass) |
| `DELETE` | `/api/v1/providers/{id}/specialties/{sId}` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | None | `ApiResponse<Void>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{id}/hospitals` | No | Public | None | `ApiResponse<List<HospitalResponseDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/providers/{id}/hospitals` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | `AssignHospitalRequestDto` | `ApiResponse<Void>` | Verified (100% Pass) |
| `DELETE` | `/api/v1/providers/{id}/hospitals/{hId}` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | None | `ApiResponse<Void>` | Verified (100% Pass) |

---

## 4. Availability & Appointment Scheduling Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `GET` | `/api/v1/providers/{providerId}/availability` | No | Public | None | `ApiResponse<List<AvailabilityResponseDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/providers/{providerId}/availability` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | `CreateAvailabilityRequestDto` | `ApiResponse<AvailabilityResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{providerId}/availability/{id}` | No | Public | None | `ApiResponse<AvailabilityResponseDto>` | Verified (100% Pass) |
| `DELETE` | `/api/v1/providers/{providerId}/availability/{id}` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | None | `ApiResponse<Void>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{providerId}/slots` | No | Public | None | `ApiResponse<List<SlotResponseDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/appointments` | Yes | `ROLE_PATIENT` | `BookAppointmentRequestDto` | `ApiResponse<AppointmentResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/appointments/me` | Yes | `ROLE_PATIENT` | None | `ApiResponse<PageResponse<AppointmentSummaryDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/appointments/{id}` | Yes | Patient / Attending Doctor / Staff | None | `ApiResponse<AppointmentResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/appointments/{id}/cancel` | Yes | Patient / Doctor / Staff | `CancelAppointmentRequestDto` | `ApiResponse<AppointmentResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/appointments/{id}/reschedule` | Yes | `ROLE_PATIENT` | `RescheduleAppointmentRequestDto` | `ApiResponse<AppointmentResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{providerId}/appointments` | Yes | Doctor / Hospital Staff | None | `ApiResponse<PageResponse<AppointmentSummaryDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/hospitals/{hospitalId}/appointments` | Yes | Hospital Staff | None | `ApiResponse<PageResponse<AppointmentSummaryDto>>` | Verified (100% Pass) |

---

## 5. Clinical Encounter, Vitals & Diagnosis Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `POST` | `/api/v1/encounters` | Yes | `ROLE_DOCTOR`, `ROLE_HOSPITAL_ADMIN` | `CreateEncounterRequestDto` | `ApiResponse<EncounterResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/encounters/{id}` | Yes | Patient / Doctor / Hospital Staff | None | `ApiResponse<EncounterResponseDto>` | Verified (100% Pass) |
| `PATCH` | `/api/v1/encounters/{id}` | Yes | Attending Doctor | `UpdateEncounterRequestDto` | `ApiResponse<EncounterResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/encounters/{id}/start` | Yes | Attending Doctor | None | `ApiResponse<EncounterResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/encounters/{id}/complete` | Yes | Attending Doctor | None | `ApiResponse<EncounterResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/encounters/{id}/cancel` | Yes | Attending Doctor | `CancelEncounterRequestDto` | `ApiResponse<EncounterResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/patients/me/encounters` | Yes | `ROLE_PATIENT` | None | `ApiResponse<PageResponse<EncounterSummaryDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/providers/{providerId}/encounters` | Yes | Attending Doctor | None | `ApiResponse<PageResponse<EncounterSummaryDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/encounters/{encounterId}/vitals` | Yes | Doctor / Clinical Staff | `CreateVitalsRequestDto` | `ApiResponse<VitalsResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/encounters/{encounterId}/vitals` | Yes | Patient / Doctor / Staff | None | `ApiResponse<List<VitalsResponseDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/encounters/{encounterId}/diagnoses` | Yes | Attending Doctor | `CreateDiagnosisRequestDto` | `ApiResponse<DiagnosisResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/encounters/{encounterId}/diagnoses` | Yes | Patient / Doctor / Staff | None | `ApiResponse<List<DiagnosisResponseDto>>` | Verified (100% Pass) |
| `PATCH` | `/api/v1/diagnoses/{diagnosisId}` | Yes | Attending Doctor | `UpdateDiagnosisRequestDto` | `ApiResponse<DiagnosisResponseDto>` | Verified (100% Pass) |

---

## 6. Electronic Prescription & Dispensing Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `POST` | `/api/v1/encounters/{encounterId}/prescriptions` | Yes | Attending Doctor | `CreatePrescriptionRequestDto` | `ApiResponse<PrescriptionResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/prescriptions/{prescriptionId}` | Yes | Patient (if finalized) / Doctor | None | `ApiResponse<PrescriptionResponseDto>` | Verified (100% Pass) |
| `PATCH` | `/api/v1/prescriptions/{prescriptionId}` | Yes | Authoring Doctor | `UpdatePrescriptionRequestDto` | `ApiResponse<PrescriptionResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/prescriptions/{prescriptionId}/finalize` | Yes | Authoring Doctor | None | `ApiResponse<PrescriptionResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/prescriptions/{prescriptionId}/cancel` | Yes | Authoring Doctor | `CancelPrescriptionRequestDto` | `ApiResponse<PrescriptionResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/prescriptions/{prescriptionId}/replacements` | Yes | Authoring Doctor | `CreatePrescriptionRequestDto` | `ApiResponse<PrescriptionResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/encounters/{encounterId}/prescriptions` | Yes | Patient (no drafts) / Doctor | None | `ApiResponse<PageResponse<PrescriptionSummaryDto>>` | Verified (100% Pass) |
| `GET` | `/api/v1/patients/me/prescriptions` | Yes | `ROLE_PATIENT` | None | `ApiResponse<PageResponse<PrescriptionSummaryDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/prescriptions/{prescriptionId}/dispensing-orders` | Yes | `ROLE_PATIENT` | `CreateDispensingOrderRequestDto` | `ApiResponse<DispensingOrderResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/dispensing-orders/{orderId}` | Yes | Patient / Pharmacy / Doctor | None | `ApiResponse<DispensingOrderResponseDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/patients/me/dispensing-orders` | Yes | `ROLE_PATIENT` | None | `ApiResponse<PageResponse<DispensingOrderResponseDto>>` | Verified (100% Pass) |
| `POST` | `/api/v1/dispensing-orders/{orderId}/cancel` | Yes | `ROLE_PATIENT` | None | `ApiResponse<DispensingOrderResponseDto>` | Verified (100% Pass) |

---

## 7. Medical Records & Longitudinal Timeline Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `GET` | `/api/v1/patients/me/medical-record` | Yes | `ROLE_PATIENT` | None | `ApiResponse<MedicalRecordSummaryDto>` | Verified (100% Pass) |
| `GET` | `/api/v1/patients/me/medical-record/timeline` | Yes | `ROLE_PATIENT` | None | `ApiResponse<PageResponse<MedicalTimelineEventDto>>` | Verified (100% Pass) |

---

## 8. AI Clinical Assistant Endpoints

| HTTP Method | Endpoint Path | Auth Required | Permitted Roles | Request Body | Response Body | Verification Status |
|---|---|---|---|---|---|---|
| `POST` | `/api/v1/ai/triage` | Yes | `ROLE_PATIENT`, `ROLE_DOCTOR` | `AiTriageRequestDto` | `ApiResponse<AiTriageResponseDto>` | Verified (100% Pass) |
| `POST` | `/api/v1/ai/prescriptions/safety-check` | Yes | `ROLE_DOCTOR`, `HOSPITAL_ADMIN`, `PATIENT` | `AiPrescriptionSafetyCheckRequestDto` | `ApiResponse<AiPrescriptionSafetyCheckResponseDto>` | Verified (100% Pass) |
