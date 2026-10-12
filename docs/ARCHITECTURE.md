# MediSimbio Backend Architecture (Phase 5)

## 1. System Overview

MediSimbio is a comprehensive healthcare technology ecosystem connecting patients, doctors, hospitals, diagnostic laboratories, pharmacies, and future government/national health infrastructure (Ayushman Bharat Digital Mission - ABDM, and HL7 FHIR interoperability).

This document details the architectural foundation and domain implementation up to **Phase 5 (Encounter + Medical Record Core)**.

```
+-------------------------------------------------------------------------------+
|                       Clients (Flutter Mobile / Web)                          |
+-------------------------------------------------------------------------------+
                                    |
                                    | HTTPS / REST (JSON)
                                    v
+-------------------------------------------------------------------------------+
|                           Spring Boot 3.4 Backend                             |
|                                                                               |
|  +-------------------------------------------------------------------------+  |
|  |                      Security Filter Foundation                         |  |
|  |  - Stateless REST Session Management                                    |  |
|  |  - JSON 401 Unauthorized / 403 Forbidden Handlers                       |  |
|  |  - DevAuthenticationFilter & AuthenticationFacade (Auth Context)        |  |
|  |  - Public Whitelist (/api/v1/health/**, /api/v1/med-id/verify/**, docs) |  |
|  |  - Public Catalog & Slots (GET /api/v1/hospitals, /providers, /slots)   |  |
|  |  - Protected Clinical Encounters, Medical Records, & Mutations          |  |
|  +-------------------------------------------------------------------------+  |
|                                   |                                           |
|  +-------------------------------------------------------------------------+  |
|  |                       REST Controllers Layer                            |  |
|  |  - PatientController: /api/v1/patients (/me, /me/med-id)                |  |
|  |  - MedIdController: /api/v1/med-id/verify/{medId}                      |  |
|  |  - SpecialtyController: /api/v1/specialties                             |  |
|  |  - HospitalController: /api/v1/hospitals                                |  |
|  |  - ProviderController: /api/v1/providers                                |  |
|  |  - AvailabilityController: /api/v1/providers/{providerId}/availability  |  |
|  |  - SlotController: /api/v1/providers/{providerId}/slots                |  |
|  |  - AppointmentController: /api/v1/appointments                          |  |
|  |  - EncounterController: /api/v1/encounters                              |  |
|  |  - DiagnosisController: /api/v1/encounters/{id}/diagnoses, /diagnoses/{id}|
|  |  - VitalsController: /api/v1/encounters/{id}/vitals                     |  |
|  |  - MedicalRecordController: /api/v1/patients/me/medical-records         |  |
|  |  - HealthController: /api/v1/health                                     |  |
|  |  - Jakarta Bean Validation (@Valid, @NotNull, @NotBlank, @Email)        |  |
|  |  - OpenAPI 3 / Swagger Documentation Generation                         |  |
|  +-------------------------------------------------------------------------+  |
|                                   |                                           |
|  +-------------------------------------------------------------------------+  |
|  |                   Centralized Exception Handling                        |  |
|  |  - GlobalExceptionHandler (@RestControllerAdvice)                       |  |
|  |  - Standardized JSON Error Envelope (timestamp, status, error, details) |  |
|  +-------------------------------------------------------------------------+  |
|                                   |                                           |
|  +-------------------------------------------------------------------------+  |
|  |                          Modular Monolith                               |  |
|  |  [ACTIVE MODULES - PHASES 1 - 5]                                        |  |
|  |  ├── patient (Profile management, demographics, DTOs, Mappers)          |  |
|  |  ├── medid (Authoritative Med-ID generator, QR tokens, Verification)    |  |
|  |  ├── specialty (Standard medical specialties registry & codes)          |  |
|  |  ├── hospital (Government & Private hospitals directory, geo search)   |  |
|  |  ├── provider (Doctor profiles, specialty mappings, affiliations)      |  |
|  |  ├── appointment (Doctor availability, dynamic slots, atomic booking)   |  |
|  |  ├── encounter (Clinical session lifecycle, status, appointment sync)   |  |
|  |  ├── diagnosis (Clinical & ICD coding, primary/secondary diagnoses)     |  |
|  |  ├── vitals (Physiological vitals measurements, automated BMI calc)     |  |
|  |  └── medicalrecord (Aggregated record overview, chronological timeline) |  |
|  |                                                                         |  |
|  |  [PLANNED FUTURE MODULES]                                               |  |
|  |  ├── auth            ├── prescription      ├── laboratory               |  |
|  |  ├── pharmacy        ├── consent           ├── emergency                |  |
|  |  ├── audit           ├── notification      ├── ai                       |  |
|  |  └── interoperability                                                   |  |
|  +-------------------------------------------------------------------------+  |
|                                   |                                           |
|  +-------------------------------------------------------------------------+  |
|  |                     Spring Data JPA & Hibernate                         |  |
|  |  - Strict DTO / Entity Isolation                                        |  |
|  |  - JPA Auditing (@CreatedDate, @LastModifiedDate)                       |  |
|  |  - Dynamic Query Specifications (HospitalSpec, ProviderSpec, ApptSpec,  |  |
|  |    EncounterSpec)                                                       |  |
|  |  - Concurrency Lock & Double-Booking Prevention                         |  |
|  |  - HikariCP Connection Pool                                             |  |
|  +-------------------------------------------------------------------------+  |
|                                   |                                           |
|  +-------------------------------------------------------------------------+  |
|  |                        Flyway Database Migrations                       |  |
|  |  - V1__initial_schema.sql (System metadata & audit verification)        |  |
|  |  - V2__create_patient_schema.sql (Patients and Med-IDs tables)          |  |
|  |  - V3__create_provider_hospital_catalog.sql (Catalog & Affiliations)    |  |
|  |  - V4__create_appointment_schema.sql (Doctor availability & appts)      |  |
|  |  - V5__create_encounter_medical_record_schema.sql (Encounters/vitals)   |  |
|  +-------------------------------------------------------------------------+  |
+-------------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------------+
|                             PostgreSQL 16+ Engine                             |
|                        Authoritative System of Record                         |
+-------------------------------------------------------------------------------+
```

---

## 2. Database Schema & Entity-Relationship Model (Phase 5)

```mermaid
erDiagram
    PATIENTS ||--o| MED_IDS : "has one"
    PROVIDERS ||--o{ PROVIDER_SPECIALTIES : "specializes in"
    SPECIALTIES ||--o{ PROVIDER_SPECIALTIES : "includes"
    PROVIDERS ||--o{ PROVIDER_HOSPITALS : "affiliated with"
    HOSPITALS ||--o{ PROVIDER_HOSPITALS : "hosts"
    PROVIDERS ||--o{ DOCTOR_AVAILABILITY : "maintains shifts"
    HOSPITALS ||--o{ DOCTOR_AVAILABILITY : "hosts shifts"
    PATIENTS ||--o{ APPOINTMENTS : "books"
    PROVIDERS ||--o{ APPOINTMENTS : "conducts"
    HOSPITALS ||--o{ APPOINTMENTS : "venues"
    SPECIALTIES ||--o{ APPOINTMENTS : "categorizes"
    APPOINTMENTS ||--o| APPOINTMENTS : "rescheduled from"
    
    PATIENTS ||--o{ ENCOUNTERS : "receives care"
    PROVIDERS ||--o{ ENCOUNTERS : "attends"
    HOSPITALS ||--o{ ENCOUNTERS : "hosts"
    APPOINTMENTS ||--o| ENCOUNTERS : "originates from"
    ENCOUNTERS ||--o{ DIAGNOSES : "contains"
    ENCOUNTERS ||--o{ ENCOUNTER_VITALS : "records"
    
    PATIENTS {
        UUID id PK
        VARCHAR(128) auth_user_id UK
        VARCHAR(150) full_name
        VARCHAR(150) email UK
        VARCHAR(20) mobile_number UK
        DATE date_of_birth
        VARCHAR(20) gender
        VARCHAR(10) blood_group
        VARCHAR(500) allergies
        VARCHAR(500) existing_conditions
        VARCHAR(150) emergency_contact_name
        VARCHAR(50) emergency_contact_relationship
        VARCHAR(20) emergency_contact_mobile
        VARCHAR(500) photo_url
        BOOLEAN profile_completed
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    MED_IDS {
        UUID id PK
        UUID patient_id FK,UK
        VARCHAR(20) med_id_number UK
        VARCHAR(20) status
        VARCHAR(255) qr_token
        TIMESTAMP issued_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    SPECIALTIES {
        UUID id PK
        VARCHAR(50) code UK
        VARCHAR(100) display_name
        VARCHAR(500) description
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    HOSPITALS {
        UUID id PK
        VARCHAR(200) name
        VARCHAR(20) hospital_type
        VARCHAR(100) registration_identifier UK
        VARCHAR(20) phone
        VARCHAR(150) email
        VARCHAR(300) address
        VARCHAR(100) city
        VARCHAR(100) state
        VARCHAR(20) postal_code
        DOUBLE_PRECISION latitude
        DOUBLE_PRECISION longitude
        VARCHAR(50) verification_status
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    PROVIDERS {
        UUID id PK
        VARCHAR(150) full_name
        VARCHAR(100) professional_registration_number UK
        VARCHAR(200) qualification
        INTEGER experience_years
        VARCHAR(500) profile_photo_url
        VARCHAR(150) email UK
        VARCHAR(20) mobile_number UK
        VARCHAR(50) verification_status
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    PROVIDER_SPECIALTIES {
        UUID provider_id FK,PK
        UUID specialty_id FK,PK
    }

    PROVIDER_HOSPITALS {
        UUID id PK
        UUID provider_id FK
        UUID hospital_id FK
        VARCHAR(100) department
        NUMERIC(10,2) consultation_fee
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    DOCTOR_AVAILABILITY {
        UUID id PK
        UUID provider_id FK
        UUID hospital_id FK
        VARCHAR(20) day_of_week
        TIME start_time
        TIME end_time
        INTEGER slot_duration_minutes
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    APPOINTMENTS {
        UUID id PK
        VARCHAR(30) booking_reference UK
        UUID patient_id FK
        UUID provider_id FK
        UUID hospital_id FK
        UUID specialty_id FK
        DATE appointment_date
        TIME start_time
        TIME end_time
        VARCHAR(20) status
        VARCHAR(500) visit_reason
        VARCHAR(500) cancellation_reason
        UUID rescheduled_from_id FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ENCOUNTERS {
        UUID id PK
        VARCHAR(30) encounter_reference UK
        UUID patient_id FK
        UUID provider_id FK
        UUID hospital_id FK
        UUID appointment_id FK
        VARCHAR(30) encounter_type
        VARCHAR(20) status
        TIMESTAMP started_at
        TIMESTAMP ended_at
        VARCHAR(500) chief_complaint
        TEXT clinical_notes
        VARCHAR(500) diagnosis_summary
        TEXT treatment_plan
        VARCHAR(500) follow_up_instructions
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    DIAGNOSES {
        UUID id PK
        UUID encounter_id FK
        VARCHAR(50) diagnosis_code
        VARCHAR(255) diagnosis_name
        VARCHAR(30) diagnosis_type
        TEXT notes
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ENCOUNTER_VITALS {
        UUID id PK
        UUID encounter_id FK
        NUMERIC(4,1) temperature
        INTEGER heart_rate
        INTEGER respiratory_rate
        INTEGER systolic_blood_pressure
        INTEGER diastolic_blood_pressure
        NUMERIC(4,1) oxygen_saturation
        NUMERIC(5,2) weight_kg
        NUMERIC(5,2) height_cm
        NUMERIC(4,1) bmi
        NUMERIC(5,1) blood_glucose
        VARCHAR(500) notes
        TIMESTAMP recorded_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
```

---

## 3. Package Structure (Current Phase 5 State)

```
src/main/java/com/medisimbio/backend/
│
├── MediSimbioApplication.java          # Spring Boot Application Entrypoint
│
├── common/                             # Cross-Cutting Shared Foundation
│   ├── audit/                          # BaseEntity & JPA Auditing
│   ├── exception/                      # Global Exception Handling & Error DTOs
│   ├── response/                       # Standard API Response Envelopes (ApiResponse, PageResponse)
│   ├── system/                         # System Foundation & Verification
│   └── util/                           # Utilities (DateTime formatting)
│
├── config/                             # Spring Configurations
│   ├── JpaAuditingConfig.java
│   ├── OpenApiConfig.java
│   └── WebMvcConfig.java
│
├── security/                           # Security & Auth Context Abstraction
│   ├── AuthenticationFacade.java       # Current user identity abstraction
│   ├── DevAuthenticationFilter.java    # Dev/test auth filter (X-User-Id / Bearer)
│   ├── RestAccessDeniedHandler.java    # Standard 403 JSON handler
│   ├── RestAuthenticationEntryPoint.java # Standard 401 JSON handler
│   ├── SecurityConfig.java             # SecurityFilterChain & Whitelist rules
│   ├── SecurityConstants.java          # Public routes & Role constants
│   ├── SecurityContextAuthenticationFacade.java
│   └── UserPrincipal.java
│
├── health/                             # Health Module
│   └── HealthController.java           # GET /api/v1/health
│
├── patient/                            # [PHASE 2] Patient Management Module
├── medid/                              # [PHASE 2] Med-ID Authority Module
├── specialty/                          # [PHASE 3] Specialty Catalog Module
├── hospital/                           # [PHASE 3] Hospital Catalog Module
├── provider/                           # [PHASE 3] Healthcare Provider Module
├── appointment/                        # [PHASE 4] Appointment & Availability Subsystem
│
├── encounter/                          # [PHASE 5] Clinical Encounter Subsystem
│   ├── controller/EncounterController.java
│   ├── dto/
│   ├── entity/
│   ├── generator/EncounterReferenceGenerator.java # ENC-YYYYMMDD-XXXX generator
│   ├── mapper/EncounterMapper.java
│   ├── repository/
│   └── service/
│
├── diagnosis/                          # [PHASE 5] Diagnosis Management Subsystem
│   ├── controller/DiagnosisController.java
│   ├── dto/
│   ├── entity/
│   ├── mapper/DiagnosisMapper.java
│   ├── repository/DiagnosisRepository.java
│   └── service/
│
├── vitals/                             # [PHASE 5] Physiological Vitals Subsystem
│   ├── controller/VitalsController.java
│   ├── dto/
│   ├── entity/EncounterVitalsEntity.java
│   ├── mapper/VitalsMapper.java        # Auto BMI calculation & BP formatting
│   ├── repository/EncounterVitalsRepository.java
│   └── service/
│
├── medicalrecord/                      # [PHASE 5] Medical Record & Timeline Subsystem
│   ├── controller/MedicalRecordController.java
│   ├── dto/
│   └── service/
│
└── [future domain packages]            # Documented with package-info.java
    ├── auth/                           ├── prescription/   ├── laboratory/
    ├── pharmacy/                       ├── consent/        ├── emergency/
    ├── audit/                          ├── notification/   ├── ai/
    └── interoperability/
```

---

## 4. Complete API Endpoints Reference (Phase 5)

| Method | Endpoint | Auth Required | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | No | Liveness & service status metadata |
| `POST`| `/api/v1/patients` | **Yes** | Creates patient profile for authenticated user |
| `GET` | `/api/v1/patients/me` | **Yes** | Retrieves current authenticated patient profile & Med-ID |
| `PATCH`| `/api/v1/patients/me` | **Yes** | Partially updates profile attributes |
| `POST`| `/api/v1/patients/me/med-id` | **Yes** | Generates or retrieves Med-ID (`MD-XXXX-XXXX`) & QR token |
| `GET` | `/api/v1/patients/me/med-id` | **Yes** | Retrieves current patient's Med-ID & QR token |
| `GET` | `/api/v1/med-id/verify/{medId}` | No | Verifies authenticity and active status of a Med-ID |
| `GET` | `/api/v1/specialties` | No | Lists standard medical specialties (paginated) |
| `GET` | `/api/v1/specialties/{id}` | No | Retrieves specialty by UUID |
| `GET` | `/api/v1/specialties/code/{code}` | No | Retrieves specialty by uppercase code |
| `POST`| `/api/v1/specialties` | **Yes** | Registers a new medical specialty |
| `GET` | `/api/v1/hospitals` | No | Search and filter hospitals by city, type, query |
| `GET` | `/api/v1/hospitals/{id}` | No | Full hospital facility profile |
| `POST`| `/api/v1/hospitals` | **Yes** | Registers a hospital / clinic facility |
| `PATCH`| `/api/v1/hospitals/{id}` | **Yes** | Updates hospital details |
| `GET` | `/api/v1/hospitals/{id}/providers` | No | Lists doctors affiliated with hospital |
| `GET` | `/api/v1/providers` | No | Search & filter doctors by specialty, hospital, city, name |
| `GET` | `/api/v1/providers/{id}` | No | Full doctor profile with specialties & affiliations |
| `POST`| `/api/v1/providers` | **Yes** | Registers a doctor / practitioner |
| `PATCH`| `/api/v1/providers/{id}` | **Yes** | Updates doctor details |
| `POST`| `/api/v1/providers/{id}/specialties` | **Yes** | Assigns specialties to doctor |
| `DELETE`| `/api/v1/providers/{id}/specialties/{specialtyId}` | **Yes** | Removes specialty from doctor |
| `GET` | `/api/v1/providers/{id}/specialties` | No | Lists specialties of doctor |
| `POST`| `/api/v1/providers/{id}/hospitals` | **Yes** | Affiliates doctor with a hospital |
| `DELETE`| `/api/v1/providers/{id}/hospitals/{hospitalId}` | **Yes** | Removes hospital affiliation |
| `GET` | `/api/v1/providers/{id}/hospitals` | No | Lists hospital affiliations of doctor |
| `POST`| `/api/v1/providers/{providerId}/availability` | **Yes** | Creates doctor recurring weekly shift window |
| `GET` | `/api/v1/providers/{providerId}/availability` | No | Lists doctor availability shift windows |
| `GET` | `/api/v1/providers/{providerId}/availability/{id}` | No | Retrieves doctor shift window by ID |
| `PUT`/`PATCH`| `/api/v1/providers/{providerId}/availability/{id}` | **Yes** | Updates doctor shift window |
| `DELETE`| `/api/v1/providers/{providerId}/availability/{id}` | **Yes** | Deletes doctor shift window |
| `GET` | `/api/v1/providers/{providerId}/slots` | No | Computes real-time bookable consultation slots for date |
| `POST`| `/api/v1/appointments` | **Yes** | Books consultation appointment for authenticated patient |
| `GET` | `/api/v1/appointments/me` | **Yes** | Retrieves paginated appointments for authenticated patient |
| `GET` | `/api/v1/appointments/me/{id}` / `/{id}` | **Yes** | Retrieves appointment details with patient data isolation |
| `POST`| `/api/v1/appointments/{id}/cancel` | **Yes** | Cancels appointment and releases slot |
| `POST`| `/api/v1/appointments/{id}/reschedule` | **Yes** | Atomically reschedules appointment to new slot |
| `GET` | `/api/v1/providers/{providerId}/appointments` | **Yes** | Directory view: Doctor appointments filtered by date/hospital |
| `GET` | `/api/v1/hospitals/{hospitalId}/appointments` | **Yes** | Directory view: Hospital facility appointments filtered by date/doctor |
| `POST`| `/api/v1/encounters` | **Yes** | Initiates new clinical encounter session |
| `GET` | `/api/v1/encounters/{id}` | **Yes** | Retrieves clinical encounter by ID with patient isolation |
| `PATCH`| `/api/v1/encounters/{id}` | **Yes** | Updates clinical notes and diagnosis summary on open encounter |
| `POST`| `/api/v1/encounters/{id}/complete` | **Yes** | Finalizes encounter and completes linked appointment |
| `POST`| `/api/v1/encounters/{id}/cancel` | **Yes** | Cancels active clinical encounter session |
| `GET` | `/api/v1/patients/me/encounters` | **Yes** | Retrieves paginated encounter list for authenticated patient |
| `GET` | `/api/v1/providers/{providerId}/encounters` | **Yes** | Directory view: Provider encounters |
| `GET` | `/api/v1/hospitals/{hospitalId}/encounters` | **Yes** | Directory view: Hospital facility encounters |
| `POST`| `/api/v1/encounters/{encounterId}/diagnoses` | **Yes** | Adds diagnosis (primary, secondary, differential) to encounter |
| `GET` | `/api/v1/encounters/{encounterId}/diagnoses` | **Yes** | Lists all diagnoses recorded for an encounter |
| `PATCH`| `/api/v1/diagnoses/{id}` | **Yes** | Modifies existing diagnosis record |
| `DELETE`| `/api/v1/diagnoses/{id}` | **Yes** | Removes diagnosis from active encounter |
| `POST`| `/api/v1/encounters/{encounterId}/vitals` | **Yes** | Records vitals with automated BMI calculation & clinical range validation |
| `GET` | `/api/v1/encounters/{encounterId}/vitals` | **Yes** | Retrieves vitals recorded during encounter |
| `GET` | `/api/v1/patients/me/medical-records` | **Yes** | Retrieves consolidated medical record overview |
| `GET` | `/api/v1/patients/me/medical-records/timeline` | **Yes** | Retrieves chronological medical events timeline |
| `GET` | `/swagger-ui.html` | No | Interactive OpenAPI 3 / Swagger documentation |
| `GET` | `/v3/api-docs` | No | OpenAPI 3 JSON specification |
