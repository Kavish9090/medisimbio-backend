# MediSimbio Backend: Phase 3 Analysis & Healthcare Provider / Hospital Catalog Architecture

## 1. Overview & Objective

In **Phase 3**, we implement the **Healthcare Provider + Hospital Catalog** subsystem:
1. **Specialty Catalog Module** (`com.medisimbio.backend.specialty`)
2. **Hospital Catalog Module** (`com.medisimbio.backend.hospital`)
3. **Healthcare Provider Module** (`com.medisimbio.backend.provider`)

This domain layer creates the authoritative registry of hospitals (Government & Private), medical specialties, and licensed healthcare practitioners (Doctors/Specialists) with their clinical affiliations and qualifications.

---

## 2. Domain Modeling & Relational Design

```
+------------------+         +-----------------------+         +-------------------+
|    Hospitals     | <-------|   ProviderHospitals   |-------> |     Providers     |
| (Govt / Private) |  1   *  |   (Affiliations)      | *    1  | (Doctors/Clinics) |
+------------------+         +-----------------------+         +-------------------+
                                                                         |
                                                                   *     |
                                                             +----------------------+
                                                             | ProviderSpecialties  |
                                                             +----------------------+
                                                                   |     *
                                                                   |  1
                                                             +-------------------+
                                                             |    Specialties    |
                                                             | (Medical Domains) |
                                                             +-------------------+
```

### Relational Mapping & Schema Specifications

#### 1. `specialties` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Immutable internal UUID |
| `code` | `VARCHAR(50)` | `NOT NULL UNIQUE` | Standard identifier (e.g. `CARDIOLOGY`, `PEDIATRICS`) |
| `display_name` | `VARCHAR(100)` | `NOT NULL` | Human readable name (e.g. `Cardiology`) |
| `description` | `VARCHAR(500)` | `NULLABLE` | Clinical specialty description |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active/Inactive status flag |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record modification timestamp |

#### 2. `hospitals` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Immutable internal UUID |
| `name` | `VARCHAR(200)` | `NOT NULL` | Full hospital/clinic name |
| `hospital_type` | `VARCHAR(20)` | `NOT NULL` | `GOVERNMENT` or `PRIVATE` |
| `registration_identifier` | `VARCHAR(100)` | `UNIQUE NULLABLE` | Official registry identifier / ROHINI / ABDM facility ID |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Primary reception / contact phone |
| `email` | `VARCHAR(150)` | `NULLABLE` | Official institutional email |
| `address` | `VARCHAR(300)` | `NOT NULL` | Physical street address |
| `city` | `VARCHAR(100)` | `NOT NULL` | City/District |
| `state` | `VARCHAR(100)` | `NOT NULL` | State/Territory |
| `postal_code` | `VARCHAR(20)` | `NOT NULL` | Postal/PIN code |
| `latitude` | `DOUBLE PRECISION` | `NULLABLE` | GPS Latitude coordinate (-90.0 to +90.0) |
| `longitude` | `DOUBLE PRECISION` | `NULLABLE` | GPS Longitude coordinate (-180.0 to +180.0) |
| `verification_status` | `VARCHAR(50)` | `NOT NULL DEFAULT 'VERIFIED'` | Facility verification state |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active/Operational status |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp |

#### 3. `providers` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Immutable internal UUID |
| `full_name` | `VARCHAR(150)` | `NOT NULL` | Doctor's full name with title |
| `professional_registration_number` | `VARCHAR(100)` | `NOT NULL UNIQUE` | State/National Medical Council registration number |
| `qualification` | `VARCHAR(200)` | `NOT NULL` | Degrees & qualifications (e.g. `MBBS, MD - Cardiology`) |
| `experience_years` | `INTEGER` | `NULLABLE DEFAULT 0` | Years of clinical practice |
| `profile_photo_url` | `VARCHAR(500)` | `NULLABLE` | Doctor profile avatar URI |
| `email` | `VARCHAR(150)` | `NOT NULL UNIQUE` | Doctor's contact/official email |
| `mobile_number` | `VARCHAR(20)` | `NOT NULL UNIQUE` | Doctor's contact mobile number |
| `verification_status` | `VARCHAR(50)` | `NOT NULL DEFAULT 'VERIFIED'` | License verification status |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Practicing status |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp |

#### 4. `provider_specialties` Table (Join Table)
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `provider_id` | `UUID` | `NOT NULL REFERENCES providers(id) ON DELETE CASCADE` | Doctor reference |
| `specialty_id` | `UUID` | `NOT NULL REFERENCES specialties(id) ON DELETE CASCADE` | Specialty reference |
| *Composite PK / Unique* | `(provider_id, specialty_id)` | `PRIMARY KEY / UNIQUE` | Enforces single mapping per specialty |

#### 5. `provider_hospitals` Table (Affiliation Entity)
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Immutable internal UUID |
| `provider_id` | `UUID` | `NOT NULL REFERENCES providers(id) ON DELETE CASCADE` | Doctor reference |
| `hospital_id` | `UUID` | `NOT NULL REFERENCES hospitals(id) ON DELETE CASCADE` | Hospital reference |
| `department` | `VARCHAR(100)` | `NULLABLE` | Clinical department inside hospital |
| `consultation_fee` | `NUMERIC(10,2)` | `NULLABLE` | Reference consultation fee (non-payment placeholder) |
| `active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active affiliation flag |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Audit timestamp |
| *Unique Constraint* | `(provider_id, hospital_id)` | `UNIQUE` | Unique active doctor-hospital pair |

---

## 3. REST API Contract Specifications

### 3.1 Specialty Endpoints (`/api/v1/specialties`)
- `GET /api/v1/specialties`
  - **Auth**: Public Read (`permitAll`)
  - **Query Params**: `active` (boolean, optional, default true), `page`, `size`, `sort`
  - **Response**: `ApiResponse<PageResponse<SpecialtyResponseDto>>`
- `GET /api/v1/specialties/{id}`
  - **Auth**: Public Read (`permitAll`)
  - **Response**: `ApiResponse<SpecialtyResponseDto>`
- `POST /api/v1/specialties`
  - **Auth**: Authenticated (Admin/Provider Management)
  - **Request Body**: `CreateSpecialtyRequestDto` (`code`, `displayName`, `description`)
  - **Response**: `ApiResponse<SpecialtyResponseDto>` (`201 Created`)

### 3.2 Hospital Endpoints (`/api/v1/hospitals`)
- `GET /api/v1/hospitals`
  - **Auth**: Public Read (`permitAll`)
  - **Query Params**: `type` (`GOVERNMENT` | `PRIVATE`), `city` (string), `state` (string), `active` (boolean), `search` (keyword across name, city, address), `page`, `size`, `sort`
  - **Response**: `ApiResponse<PageResponse<HospitalSummaryDto>>`
- `GET /api/v1/hospitals/{id}`
  - **Auth**: Public Read (`permitAll`)
  - **Response**: `ApiResponse<HospitalResponseDto>` (includes facility details, coordinates, contacts)
- `POST /api/v1/hospitals`
  - **Auth**: Authenticated
  - **Request Body**: `CreateHospitalRequestDto`
  - **Response**: `ApiResponse<HospitalResponseDto>` (`201 Created`)
- `PATCH /api/v1/hospitals/{id}`
  - **Auth**: Authenticated
  - **Request Body**: `UpdateHospitalRequestDto` (partial update)
  - **Response**: `ApiResponse<HospitalResponseDto>` (`200 OK`)
- `GET /api/v1/hospitals/{id}/providers`
  - **Auth**: Public Read (`permitAll`)
  - **Query Params**: `active` (boolean), `page`, `size`
  - **Response**: `ApiResponse<PageResponse<ProviderSummaryDto>>`

### 3.3 Provider Endpoints (`/api/v1/providers`)
- `GET /api/v1/providers`
  - **Auth**: Public Read (`permitAll`)
  - **Query Params**: `specialtyId` (UUID), `hospitalId` (UUID), `city` (string), `active` (boolean), `search` (name/qualification), `page`, `size`, `sort`
  - **Response**: `ApiResponse<PageResponse<ProviderSummaryDto>>`
- `GET /api/v1/providers/{id}`
  - **Auth**: Public Read (`permitAll`)
  - **Response**: `ApiResponse<ProviderResponseDto>` (includes specialty list and affiliated hospital list)
- `POST /api/v1/providers`
  - **Auth**: Authenticated
  - **Request Body**: `CreateProviderRequestDto`
  - **Response**: `ApiResponse<ProviderResponseDto>` (`201 Created`)
- `PATCH /api/v1/providers/{id}`
  - **Auth**: Authenticated
  - **Request Body**: `UpdateProviderRequestDto`
  - **Response**: `ApiResponse<ProviderResponseDto>` (`200 OK`)
- `POST /api/v1/providers/{id}/specialties`
  - **Auth**: Authenticated
  - **Request Body**: `AssignSpecialtyRequestDto` (`specialtyIds`: Set<UUID>)
  - **Response**: `ApiResponse<ProviderResponseDto>`
- `DELETE /api/v1/providers/{id}/specialties/{specialtyId}`
  - **Auth**: Authenticated
  - **Response**: `ApiResponse<Void>`
- `GET /api/v1/providers/{id}/specialties`
  - **Auth**: Public Read (`permitAll`)
  - **Response**: `ApiResponse<List<SpecialtyResponseDto>>`
- `POST /api/v1/providers/{id}/hospitals`
  - **Auth**: Authenticated
  - **Request Body**: `AssignHospitalRequestDto` (`hospitalId`, `department`, `consultationFee`, `active`)
  - **Response**: `ApiResponse<ProviderHospitalResponseDto>`
- `DELETE /api/v1/providers/{id}/hospitals/{hospitalId}`
  - **Auth**: Authenticated
  - **Response**: `ApiResponse<Void>`
- `GET /api/v1/providers/{id}/hospitals`
  - **Auth**: Public Read (`permitAll`)
  - **Response**: `ApiResponse<List<ProviderHospitalResponseDto>>`

---

## 4. Security & Access Boundaries

- **Public Access**: All `GET` requests under `/api/v1/hospitals/**`, `/api/v1/providers/**`, and `/api/v1/specialties/**` are publicly accessible (`permitAll()`) so mobile clients and visitors can search for doctors and hospitals without mandatory sign-in.
- **Protected Mutations**: All state-modifying actions (`POST`, `PATCH`, `PUT`, `DELETE`) require authentication via Spring Security.

---

## 5. Future Integration Points (Post-Phase 3)

1. **Appointments (Phase 4)**: Will reference `ProviderHospitalEntity` and `ProviderEntity` to schedule visits without duplicating doctor or facility data.
2. **Doctor Availability & Slot Engine (Phase 4)**: Will attach weekly/daily recurring schedules to `(provider_id, hospital_id)` pairs.
3. **FHIR / ABDM Interoperability**: `HospitalEntity` maps directly to FHIR `Organization` and `Location` resources; `ProviderEntity` maps directly to FHIR `Practitioner` and `PractitionerRole`.
