# MediSimbio Flutter Application & Backend Analysis (Phase 1)

## 1. Executive Summary

This document presents a comprehensive architectural and technical analysis of the existing **MediSimbio Flutter Application** (`medisimbio-main`) located in `D:\medsimbio`. 

The Flutter application was developed as a frontend prototype integrated with **Firebase Authentication**, **Cloud Firestore**, and local device preferences (**SharedPreferences**). 

The goal of this analysis is to establish the authoritative reference for building the **MediSimbio Spring Boot / PostgreSQL Backend**, ensuring that:
1. No existing Flutter functionality or files are broken or prematurely modified during Phase 1.
2. The domain model, business rules, and API contracts required by the mobile app are fully understood and mapped to a future enterprise-grade relational schema.
3. The boundary between current Phase 1 foundation work and future business module implementation is clearly demarcated.

---

## 2. Flutter Project Architecture & Inventory

### 2.1 Project Metadata
- **Package Name**: `medisimbio_ui`
- **Flutter SDK**: `>=3.4.0 <4.0.0`
- **State Management & Architecture**: Stateful widgets, Provider/StreamBuilder with Firestore listeners, Repository pattern for mocked/in-memory data.
- **Root Entrypoint**: `lib/main.dart` initialized with `Firebase.initializeApp()`.
- **Target Platforms**: Android (`com.example.medisimbio` / `com.example.medisimbio_ui`), iOS, Web.

### 2.2 Dependencies (`pubspec.yaml`)
| Dependency | Version | Purpose in Flutter App |
| :--- | :--- | :--- |
| `firebase_core` | `^4.15.0` | Core Firebase SDK Initialization |
| `firebase_auth` | `^6.7.0` | User authentication (Email/Password, Phone OTP, Google OAuth) |
| `cloud_firestore` | `^6.10.0` | NoSQL document database for user profiles, appointments, records |
| `google_sign_in` | `^6.2.2` | Google OAuth token retrieval |
| `shared_preferences`| `^2.5.2` | Local persistent state (e.g. `has_seen_onboarding`) |
| `qr_flutter` | `^4.1.0` | QR code rendering for Med-ID |
| `url_launcher` | `^6.3.0` | External navigation for phone calls, maps, web links |

---

## 3. Data Storage & Firestore Collections Analysis

The current Flutter app uses Firestore as its primary remote database with the following structure:

```
cloud_firestore
│
├── users/{uid}                                (Patient Profile Document)
│   │
│   ├── appointments/{appointmentId}           (Appointments Subcollection)
│   ├── consentRecords/{accessRequestId}       (Granular Consent Requests & Grants)
│   ├── prescriptions/{id}                     (Prescription Records)
│   ├── labReports/{id}                        (Laboratory & Diagnostic Reports)
│   ├── consultations/{id}                     (Doctor Consultation Records)
│   ├── clinicalNotes/{id}                     (Clinical Notes)
│   ├── documents/{id}                         (Patient Medical Documents)
│   ├── pharmacyRecords/{id}                   (Pharmacy Dispensation Records)
│   └── healthIntakes/{id}                     (AI Symptom & Triage Intake Records)
```

### 3.1 Document Schemas & Inferred Entity Definitions

#### A. Patient Profile (`users/{uid}`)
- **Model**: `lib/models/patient_profile.dart`
- **Fields**:
  - `uid` (String): Unique identifier (Firebase UID)
  - `name` (String): Full patient name
  - `email` (String): Contact email
  - `mobile` (String): Contact phone number
  - `dob` (String): Date of birth (formatted as string)
  - `gender` (String): Gender identity
  - `medId` (String): Unique MediSimbio Health ID (e.g. `MD-XXXX-XXXX`)
  - `bloodGroup` (String): Blood group (e.g. `O+`, `B-`)
  - `allergies` (String): Known drug/environmental allergies
  - `existingConditions` (String): Chronic medical conditions
  - `emergencyName` (String): Emergency contact name
  - `emergencyRelationship` (String): Relationship to emergency contact
  - `emergencyMobile` (String): Emergency contact phone number
  - `photoUrl` (String?): Profile avatar URI
  - `profileCompleted` (bool): Flag indicating onboarding completion
  - `createdAt`, `updatedAt` (Timestamp): Server timestamps

#### B. Doctor Entity (In-Memory Catalog)
- **Model**: `lib/models/doctor.dart`
- **Service**: `lib/services/doctor_repository.dart`
- **Fields**:
  - `id` (String): Doctor identifier
  - `name` (String): Doctor full name
  - `specialty` (String): Medical specialty (e.g., Cardiology, General Medicine, Dermatology)
  - `hospitalName` (String): Affiliated healthcare facility
  - `rating` (double): Average doctor rating (e.g., 4.9)
  - `reviewCount` (int): Number of patient reviews
  - `experienceYears` (int): Years of medical practice
  - `consultationFee` (double): Base consultation charge
  - `imageUrl` (String): Doctor image URL
  - `about` (String): Professional bio and qualifications
  - `availableDates` (List<String>): Available consultation dates
  - `availableSlots` (List<String>): Time slots
  - `consultationMode` (String): `In-Person`, `Video`, `Both`

#### C. Appointment Entity (`users/{uid}/appointments/{appointmentId}`)
- **Model**: `lib/models/appointment.dart`
- **Service**: `lib/services/appointment_service.dart`
- **Fields**:
  - `id` (String): Appointment identifier
  - `uid` (String): Patient UID
  - `doctorId` (String), `doctorName` (String), `specialty` (String), `hospitalName` (String)
  - `dateTime` (String): Scheduled date and time
  - `consultationType` (String): `In-Person`, `Video Consultation`
  - `status` (String): `PENDING`, `CONFIRMED`, `Upcoming`, `RESCHEDULED`, `CANCELLED`, `COMPLETED`, `CHECKED_IN`, `WAITING`, `IN_CONSULTATION`
  - `consultationFee` (double): Charged fee
  - `isLiveTrackingActive` (bool): Queue tracking active status
  - `tokenNumber` (String?): Patient token number in queue
  - `currentServingToken` (String?): Currently served token at clinic
  - `queuePosition` (int?): Current position in waiting queue
  - `estimatedWaitMinutes` (int?): Live estimated wait duration
  - `trackingLastUpdated` (DateTime?): Queue update timestamp
  - `hospitalAddress` (String?), `latitude` (double?), `longitude` (double?)
  - `createdAt` (DateTime?)

#### D. Consent Record (`users/{uid}/consentRecords/{accessRequestId}`)
- **Model**: `lib/models/consent_record.dart`
- **Service**: `lib/services/consent_service.dart`
- **Fields**:
  - `accessRequestId` (String): Unique consent request identifier
  - `patientId` (String): Patient UID
  - `requesterId` (String): Requesting doctor / organization ID
  - `requesterType` (String): `PROVIDER`, `HOSPITAL`, `LAB`, `PHARMACY`
  - `requesterName` (String): Display name of requester
  - `organizationId`, `organizationName` (String?)
  - `requestedScopes` (List<String>): Scopes requested (`MEDICAL_HISTORY`, `PRESCRIPTIONS`, `LAB_REPORTS`, `DOCTOR_CONSULTATIONS`, `CLINICAL_NOTES`, `DOCUMENTS`, `MEDICATION_RECORDS`, `APPOINTMENTS`)
  - `grantedScopes` (List<String>): Scopes approved by patient
  - `requestedAt` (DateTime), `durationHours` (int?), `expiresAt` (DateTime?)
  - `status` (String): `PENDING`, `GRANTED`, `DENIED`, `REVOKED`, `EXPIRED`
  - `grantedAt`, `deniedAt`, `revokedAt`, `createdAt`, `updatedAt` (DateTime?)
  - `reason` (String?): Clinical justification for request

#### E. Medical Records (`prescriptions`, `labReports`, `consultations`, `clinicalNotes`, `documents`, `pharmacyRecords`)
- **Model**: `lib/models/medical_record.dart`
- **Service**: `lib/services/medical_record_service.dart`
- **Prescription**: Contains `medicines` list (`medicineName`, `dosage`, `frequency`, `duration`, `timing`, `notes`), `doctorId`, `doctorName`, `clinicName`, `prescriptionDate`, `status`, `provenance`.
- **Lab Report**: Contains `testName`, `testCode`, `laboratory`, `laboratoryId`, `orderingProvider`, `orderDate`, `sampleCollectionDate`, `processingDate`, `reportAvailableDate`, `status`, `reportUrl`, `summary`.
- **Pharmacy Record**: Links to `prescriptionId`, `pharmacyId`, `pharmacyName`, `medicines`, `status` (`PENDING`, `VERIFIED`, `DISPENSED`), timestamps.
- **Consultations / Clinical Notes / Documents**: Categorized clinical notes, consultation transcripts, and attached medical files.

#### F. AI Intake Record (`users/{uid}/healthIntakes/{id}`)
- **Model**: `lib/models/ai_intake.dart`
- **Service**: `lib/services/ai_health_service.dart`
- **Fields**: `id`, `uid`, `chiefComplaint`, `duration`, `associatedSymptoms`, `severity`, `additionalNotes`, `createdAt`, `status` (`AI Structured - Pending Verification`).

---

## 4. Architectural Analysis: Inferred Backend Requirements

### 4.1 System of Record Transition (Firebase → PostgreSQL)
In the existing Flutter application, data is stored client-side or written directly to Firestore subcollections without backend transactional guarantees, schema validation, or relational constraints. 

In the authoritative backend architecture:
1. **PostgreSQL 16+** serves as the authoritative system of record.
2. Relational normalization will replace nested document subcollections:
   - Separate tables for `patients`, `doctors`, `facilities`, `appointments`, `prescriptions`, `prescription_items`, `lab_reports`, `consent_records`, `audit_logs`.
   - Foreign key relationships enforcing referential integrity.
   - Database-level constraint validation (unique constraints on Med-ID, email, phone).
3. **Med-ID Authority**: The backend must become the authoritative issuer of Med-IDs (`MD-XXXX-XXXX`), preventing collisions and binding them immutably to verified patient identities.
4. **Authoritative Consent & Emergency Engine**: Consent grants and Emergency "Break-Glass" access must be strictly validated by the backend before releasing clinical records, backed by immutable audit trails.

---

## 5. Status Matrix: Implemented vs Future vs Out of Scope for Phase 1

| Component / Feature | Flutter App Status | Spring Boot Phase 1 Status | Future Phase Target |
| :--- | :--- | :--- | :--- |
| **Backend Core Foundation** | N/A | **IMPLEMENTED (Phase 1)** | Foundation Complete |
| **Java 21 / Spring Boot 3** | N/A | **IMPLEMENTED (Phase 1)** | Foundation Complete |
| **PostgreSQL & Flyway Migrations** | N/A | **IMPLEMENTED (Phase 1)** | Multi-table schema evolution |
| **Global Error Handling & Validation** | N/A | **IMPLEMENTED (Phase 1)** | Module-specific validation rules |
| **Security Foundation (Stateless REST)**| Firebase Auth | **IMPLEMENTED (Phase 1)** | OIDC / JWT / OAuth2 Resource Server |
| **Health API (`/api/v1/health`)** | N/A | **IMPLEMENTED (Phase 1)** | Actuator + DB health indicators |
| **OpenAPI / Swagger Documentation** | N/A | **IMPLEMENTED (Phase 1)** | Full API contracts specification |
| **Patient Profile Management** | UI + Firestore | Not in Phase 1 | Phase 2 (Patient Module) |
| **Med-ID Authority & QR Code API** | UI + Local Gen | Not in Phase 1 | Phase 2 (Med-ID Module) |
| **Doctor & Hospital Management** | In-Memory Mock | Not in Phase 1 | Phase 3 (Provider Module) |
| **Appointment Booking & Queue Tracking**| UI + Firestore | Not in Phase 1 | Phase 3 (Appointment Module) |
| **Prescriptions & Lab Reports** | UI + Firestore | Not in Phase 1 | Phase 4 (Clinical Records) |
| **Consent Management & Break-Glass** | UI + Firestore | Not in Phase 1 | Phase 5 (Consent & Audit) |
| **ABDM / ABHA / FHIR Interop** | UI placeholders | Not in Phase 1 | Phase 6 (Interoperability) |
| **AI Triage & Clinical Assistant** | UI Wizard | Not in Phase 1 | Phase 7 (AI Service Integration) |

---

## 6. Phase 1 Boundary Guarantee

1. **Zero Flutter Modification**: The existing Flutter codebase in `medisimbio-main` is completely untouched and continues to run independently against Firebase.
2. **Clean Backend Isolation**: The backend is contained entirely in `medisimbio-backend/`.
3. **Strict Scope Enforcement**: No business endpoints, fake data, or premature database tables are introduced beyond the minimal foundation required to verify Spring Boot, JPA, Flyway, and PostgreSQL integration.
