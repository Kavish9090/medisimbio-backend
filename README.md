# MediSimbio Backend

> **Modular Monolith Backend for the MediSimbio Healthcare Ecosystem**

---

## 1. Overview

**MediSimbio** is a comprehensive healthcare technology platform connecting patients, healthcare providers, hospitals, diagnostic centers, pharmacies, and national health authorities (such as Ayushman Bharat Digital Mission - ABDM, and HL7 FHIR interoperability).

This repository contains the authoritative **Spring Boot 3 / PostgreSQL** backend.

---

## 2. Technology Stack

- **Language & Runtime**: Java 21 LTS (OpenJDK / Eclipse Temurin)
- **Framework**: Spring Boot 3.4.3
- **Web & Validation**: Spring Web MVC, Jakarta Bean Validation (Hibernate Validator)
- **Data Persistence**: Spring Data JPA, Hibernate 6.6
- **Database Engine**: PostgreSQL 16+
- **Database Migrations**: Flyway 10+
- **Security**: Spring Security 6 (Stateless REST, `AuthenticationFacade`, `DevAuthenticationFilter` profile-isolated)
- **Observability**: Spring Boot Actuator
- **API Documentation**: Springdoc OpenAPI 3 / Swagger UI
- **Build Tool**: Gradle 9.5 (with Gradle Wrapper)
- **Testing**: JUnit 5, AssertJ, Spring Boot Test, MockMvc, H2 (test profile)
- **Containerization**: Docker Compose (PostgreSQL 16)

---

## 3. Implemented Modules (Phases 1–6)

### 1. Patient Management (`com.medisimbio.backend.patient`)
- Authoritative patient profile storage in PostgreSQL `patients` table.
- Patient creation with validation on email, mobile, and birthdate.
- Self-service endpoints (`GET /api/v1/patients/me`, `PATCH /api/v1/patients/me`).
- Strict DTO/Entity isolation and JPA auditing.

### 2. Med-ID Authority (`com.medisimbio.backend.medid`)
- Server-side cryptographic generation of unique Med-IDs (`MD-XXXX-XXXX`).
- 1-to-1 foreign key relationship to patient with `UNIQUE` database constraint.
- Tamper-evident QR code token generation.
- Public verification endpoint (`GET /api/v1/med-id/verify/{medId}`) returning masked identity.

### 3. Specialty Catalog (`com.medisimbio.backend.specialty`)
- Medical specialties registry (e.g., Cardiology, Dermatology, Pediatrics).
- Seeded standard clinical specialties in migration.
- Public endpoints for discovery and listing.

### 4. Hospital Catalog (`com.medisimbio.backend.hospital`)
- Registry of Government and Private hospitals and clinics.
- Geocoordinates, contact information, official facility identifiers.
- Dynamic filtering by city, state, type, and keyword search.

### 5. Healthcare Provider Management (`com.medisimbio.backend.provider`)
- Medical practitioner profiles with Medical Council registration numbers.
- Many-to-many associations with medical specialties.
- Multi-facility hospital affiliations with departments and consultation fees.
- Dynamic doctor directory search by specialty, hospital, city, and qualifications.

### 6. Appointment & Availability Engine (`com.medisimbio.backend.appointment`)
- Provider weekly recurring shift availability windows and slot duration rules.
- Real-time bookable consultation slot calculation with collision checking.
- Atomic appointment booking with PostgreSQL partial unique index double-booking protection.
- Appointment lifecycle transitions (`SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
- Protected doctor and hospital appointment schedules with caller identity verification.

### 7. Clinical Encounter Subsystem (`com.medisimbio.backend.encounter`)
- Actual clinical interaction sessions created from scheduled appointments (`ENC-YYYYMMDD-XXXX`).
- Lifecycle state transitions (`OPEN` -> `IN_PROGRESS` -> `COMPLETED` / `CANCELLED`).
- Automatic synchronization completing linked appointment upon encounter completion.
- IDOR access controls for patients, attending doctors, and hospital administrators.

### 8. Clinical Diagnosis Management (`com.medisimbio.backend.diagnosis`)
- Provider-entered clinical diagnoses (`PRIMARY`, `SECONDARY`, `PROVISIONAL`, `RULE_OUT`).
- ICD-10 standard coding and status tracking (`ACTIVE`, `RESOLVED`, `INACTIVE`).
- Strict safety guarantee: zero AI-generated inference in authoritative records.

### 9. Physiological Vitals Subsystem (`com.medisimbio.backend.vitals`)
- Encounter vitals recording (temperature, heart rate, BP, SpO2, weight, height, blood glucose).
- Automated BMI calculation engine with rounding and precision handling.
- Structural boundary validation preventing physically invalid data.

### 10. Medical Record & Timeline Subsystem (`com.medisimbio.backend.medicalrecord`)
- Consolidated medical record aggregation read model over authoritative PostgreSQL domain data.
- Paginated chronological clinical events timeline (`ENCOUNTER`, `DIAGNOSIS`, `VITALS`, `PRESCRIPTION`).

### 11. E-Prescription Subsystem (`com.medisimbio.backend.prescription`)
- Authoritative electronic prescription issuance for clinical consultation sessions (`RX-YYYYMMDD-XXXX`).
- Two-stage lifecycle: `DRAFT` (provider draft) -> `FINALIZED` (immutable, clinical signature).
- Controlled cancellation workflow (`CANCELLED`) and atomic amendment workflow (`SUPERSEDED`).
- Structured medication items with dosage forms, routes, frequencies, durations, and refill limits.
- Patient privacy protection: draft prescriptions hidden from patients; patient access strictly read-only on finalized prescriptions.

### 12. Medicine Dispensing Orders Subsystem (`com.medisimbio.backend.dispensing`)
- Patient dispensing orders created against finalized prescriptions (`DSP-YYYYMMDD-XXXX`).
- Order fulfillment lifecycle (`PENDING`, `ACCEPTED`, `PARTIALLY_DISPENSED`, `DISPENSED`, `CANCELLED`, `REJECTED`).
- Concurrency protection preventing multiple duplicate active orders for the same prescription.
- Strict patient and hospital authorization boundaries with zero IDOR exposure.

---

## 4. How to Run Locally

### Step 1: Start PostgreSQL (Port 5433)
```powershell
docker compose up -d
```

### Step 2: Run Spring Boot Backend (Port 8081)
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

### Step 3: Run Full Test Suite
```powershell
.\gradlew.bat test
```

---

## 5. Security & Authentication

During development, the `DevAuthenticationFilter` (active only in `local`, `test`, `dev`, `default` profiles) establishes authenticated user identity via headers:
- `X-User-Id: <user-uid>`
- or `Authorization: Bearer <user-uid>`

In production (`application-production.yml`), `DevAuthenticationFilter` is strictly disabled, and standard OAuth2 resource server / verified JWT tokens establish identity.
