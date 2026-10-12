# MediSimbio Backend: Phase 2 Analysis & Domain Mapping

## 1. Overview & Objective

In **Phase 2**, we implement the first two authoritative healthcare business modules:
1. **Patient Management Module** (`com.medisimbio.backend.patient`)
2. **Med-ID Authority Module** (`com.medisimbio.backend.medid`)

This document establishes the precise architectural mapping between the existing Flutter application models (`medisimbio-main`), the backend Data Transfer Objects (DTOs), the JPA Entities, the relational database tables, and the REST API contracts.

---

## 2. Domain Mapping Matrix

```
Flutter Model (lib/models/patient_profile.dart)
       ↓
Backend DTOs (CreatePatientRequest, UpdatePatientRequest, PatientResponse)
       ↓
JPA Entity (PatientEntity, MedIdEntity)
       ↓
PostgreSQL Tables (patients, med_ids)
       ↓
REST API Endpoints (/api/v1/patients/me, /api/v1/patients/me/med-id)
```

### Detailed Field Mapping Table

| Flutter Model Field | DTO Field | JPA Entity Field | DB Column Name | Type / Constraints | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| N/A (Internal) | `id` | `id` | `id` | `UUID PRIMARY KEY` | Immutable internal UUID |
| `uid` (Firebase UID) | `authUserId` | `authUserId` | `auth_user_id` | `VARCHAR(128) UNIQUE NOT NULL` | Auth Provider Subject ID |
| `name` | `fullName` / `name` | `fullName` | `full_name` | `VARCHAR(150) NOT NULL` | Patient Full Name |
| `email` | `email` | `email` | `email` | `VARCHAR(150) UNIQUE NOT NULL` | Verified Contact Email |
| `mobile` | `mobileNumber` / `mobile` | `mobileNumber` | `mobile_number` | `VARCHAR(20) UNIQUE NOT NULL` | Contact Mobile Phone |
| `dob` | `dateOfBirth` / `dob` | `dateOfBirth` | `date_of_birth` | `DATE NOT NULL` | ISO-8601 (YYYY-MM-DD) |
| `gender` | `gender` | `gender` | `gender` | `VARCHAR(20) NOT NULL` | `MALE`, `FEMALE`, `OTHER` |
| `bloodGroup` | `bloodGroup` | `bloodGroup` | `blood_group` | `VARCHAR(10)` | e.g. `O+`, `B+`, `A-` |
| `allergies` | `allergies` | `allergies` | `allergies` | `VARCHAR(500)` | Profile summary notes |
| `existingConditions`| `existingConditions` | `existingConditions`| `existing_conditions` | `VARCHAR(500)` | Profile summary notes |
| `emergencyName` | `emergencyContactName` | `emergencyContactName`| `emergency_contact_name` | `VARCHAR(150)` | Emergency Contact Full Name |
| `emergencyRelationship`| `emergencyContactRelationship` | `emergencyContactRelationship` | `emergency_contact_relationship` | `VARCHAR(50)` | e.g. `Spouse`, `Parent` |
| `emergencyMobile` | `emergencyContactMobile` | `emergencyContactMobile` | `emergency_contact_mobile` | `VARCHAR(20)` | Emergency Phone Number |
| `photoUrl` | `photoUrl` | `photoUrl` | `photo_url` | `VARCHAR(500)` | Profile Avatar URI |
| `profileCompleted` | `profileCompleted` | `profileCompleted` | `profile_completed` | `BOOLEAN NOT NULL DEFAULT FALSE` | Onboarding completion flag |
| `medId` | `medId` | (Linked `MedIdEntity`) | `med_ids.med_id_number` | `VARCHAR(20) UNIQUE NOT NULL` | Format: `MD-XXXX-XXXX` |
| `createdAt` | `createdAt` | `createdAt` | `created_at` | `TIMESTAMP NOT NULL` | Audit timestamp |
| `updatedAt` | `updatedAt` | `updatedAt` | `updated_at` | `TIMESTAMP NOT NULL` | Audit timestamp |

---

## 3. Med-ID Authority Design

In the existing Flutter application, Med-IDs were generated client-side using `MedIdService.generateMedId()` in the format `MD-XXXX-XXXX` without server-side collision checks or cryptographic verification.

In Phase 2:
1. **Authoritative Generation**: The backend becomes the sole authority for issuing Med-IDs.
2. **Format Guarantee**: Pattern `^MD-[A-Z0-9]{4}-[A-Z0-9]{4}$`.
3. **Database Uniqueness**: Enforced via a `UNIQUE` database constraint on `med_ids.med_id_number` and a 1-to-1 foreign key relationship to `patients.id`.
4. **Idempotent Issuance**: Calling `POST /api/v1/patients/me/med-id` for a patient who already has an active Med-ID safely returns the existing record rather than creating duplicates.
5. **QR Code Verification Token**: Generates a verifiable token containing the Med-ID, patient UUID, and issuance metadata.

---

## 4. REST API Contract Specifications

### 4.1 Patient APIs

#### 1. `POST /api/v1/patients`
- **Purpose**: Creates the patient profile for the currently authenticated user.
- **Request Body**:
```json
{
  "fullName": "Johnathan Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+919876543210",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "bloodGroup": "O+",
  "allergies": "Penicillin",
  "existingConditions": "Hypertension",
  "emergencyContactName": "Jane Doe",
  "emergencyContactRelationship": "Spouse",
  "emergencyContactMobile": "+919876543211",
  "photoUrl": "https://example.com/photos/john.jpg"
}
```
- **Responses**:
  - `201 Created`: Returns `ApiResponse<PatientResponseDto>`
  - `400 Bad Request`: Validation failure (invalid email, date in future, etc.)
  - `401 Unauthorized`: Unauthenticated
  - `409 Conflict`: Patient profile already exists for authenticated user, or email/mobile already registered

#### 2. `GET /api/v1/patients/me`
- **Purpose**: Retrieves the patient profile of the currently authenticated user.
- **Responses**:
  - `200 OK`: Returns `ApiResponse<PatientResponseDto>` (includes assigned `medId` if issued)
  - `401 Unauthorized`: Unauthenticated
  - `404 Not Found`: Patient profile not yet created for this user

#### 3. `PATCH /api/v1/patients/me`
- **Purpose**: Partially updates profile attributes (e.g., blood group, emergency contact, avatar, completion status).
- **Request Body**:
```json
{
  "bloodGroup": "O+",
  "emergencyContactName": "Jane Doe",
  "emergencyContactRelationship": "Spouse",
  "emergencyContactMobile": "+919876543211",
  "profileCompleted": true
}
```
- **Responses**:
  - `200 OK`: Returns updated `ApiResponse<PatientResponseDto>`
  - `400 Bad Request`: Validation failure
  - `401 Unauthorized`: Unauthenticated
  - `404 Not Found`: Patient profile not found

---

### 4.2 Med-ID APIs

#### 1. `POST /api/v1/patients/me/med-id`
- **Purpose**: Generates and assigns a new authoritative Med-ID for the authenticated patient (or returns existing active Med-ID).
- **Responses**:
  - `200 OK` / `201 Created`: Returns `ApiResponse<MedIdResponseDto>`:
```json
{
  "success": true,
  "message": "Med-ID generated successfully",
  "data": {
    "medId": "MD-S3AK-9Q8P",
    "patientId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "patientName": "Johnathan Doe",
    "status": "ACTIVE",
    "qrToken": "MEDID:MD-S3AK-9Q8P:9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d:1728124800",
    "issuedAt": "2026-10-05T11:00:00Z"
  },
  "timestamp": "2026-10-05T11:00:00.123Z"
}
```

#### 2. `GET /api/v1/patients/me/med-id`
- **Purpose**: Retrieves the authenticated patient's current Med-ID and QR verification details.
- **Responses**:
  - `200 OK`: Returns `ApiResponse<MedIdResponseDto>`
  - `404 Not Found`: No Med-ID generated yet for this patient

#### 3. `GET /api/v1/med-id/verify/{medId}`
- **Purpose**: Verifies validity and active status of a Med-ID (used for clinic check-ins and QR code scans).
- **Responses**:
  - `200 OK`: Returns `ApiResponse<MedIdVerificationResponseDto>` (active status, patient name, masked details)
  - `404 Not Found`: Invalid or non-existent Med-ID

---

## 5. Security & Authorization Architecture

- **User Resolution**: The authenticated user's identity (`authUserId`) is extracted directly from the Spring Security `SecurityContext` via an `AuthenticationFacade`. Clients cannot specify arbitrary `patientId` or `authUserId` to access someone else's data.
- **Zero Privacy Leakage**: Self-service endpoints strictly resolve `me` -> `authenticatedUser`.
