# MediSimbio Backend: Comprehensive Security & Threat Model Review

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\SECURITY_REVIEW.md`  
**Date**: October 2026  
**Standards Aligned**: OWASP API Security Top 10 (2023)  

---

## 1. OWASP API Security Top 10 Analysis & Countermeasures

### 1.1 API1:2023 - Broken Object Level Authorization (BOLA / IDOR)
- **Threat**: An attacker queries or modifies records belonging to another patient or doctor by manipulating URL path UUIDs.
- **Countermeasure**: 
  - Every patient endpoint (`/api/v1/patients/me/**`, `/appointments/me`, `/medical-record/timeline`) resolves the patient's database ID directly from the authenticated `UserPrincipal` via `SecurityContextAuthenticationFacade`.
  - Doctor actions (`start`, `complete`, `prescriptions`, `diagnoses`) verify that `encounter.getProvider().getAuthUserId().equals(currentUserId)`.
  - Tested across `PatientControllerTest`, `EncounterControllerTest`, and `PrescriptionControllerTest`.

### 1.2 API2:2023 - Broken Authentication
- **Threat**: Forged identities, session hijacking, or credential stuffing.
- **Countermeasure**:
  - Stateless architecture (`SessionCreationPolicy.STATELESS`).
  - `DevAuthenticationFilter` is strictly annotated `@Profile({"local", "test", "dev", "default"})`. Verified via `DevAuthenticationBoundaryTest` that `X-User-Id` and `X-User-Role` headers are rejected with HTTP 401 in `production`.
  - Production roadmap targets OIDC / OAuth2 RS256 token verification.

### 1.3 API3:2023 - Broken Object Property Level Authorization (Mass Assignment)
- **Threat**: Callers pass sensitive internal fields (e.g. `authUserId`, `createdAt`, `status`, `medId`) during creation or updates.
- **Countermeasure**:
  - Strict separation between JPA entities and DTOs.
  - Controllers only accept explicit request DTOs (`BookAppointmentRequestDto`, `CreatePrescriptionRequestDto`).
  - Mappers (`PrescriptionMapper`, `AppointmentMapper`, `PatientMapper`) control field assignment explicitly.

### 1.4 API4:2023 - Unrestricted Resource Consumption (DoS)
- **Threat**: Large payloads, unbounded database queries, or runaway pagination exhausting server memory.
- **Countermeasure**:
  - All collection endpoints enforce pagination via Spring Data's `Pageable` and `@PageableDefault(size = 20)`.
  - HikariCP database pool limits active connections (`maximum-pool-size: 10/20`, `connection-timeout: 20000ms`).
  - Spring Boot request body size limits enforced.

### 1.5 API5:2023 - Broken Function Level Authorization (BFLA)
- **Threat**: Regular patients invoking administrative endpoints (e.g., registering hospitals or creating doctor profiles).
- **Countermeasure**:
  - Sensitive administrative endpoints require `ROLE_HOSPITAL_ADMIN` or `ROLE_SYSTEM_ADMIN`.
  - Method security (`@EnableMethodSecurity`, `@PreAuthorize`) enforces role boundaries at service and controller boundaries.

### 1.6 API6:2023 - Unrestricted Access to Sensitive Business Flows
- **Threat**: Double-booking consultation slots or submitting concurrent duplicate medicine dispensing orders.
- **Countermeasure**:
  - Appointment double-booking prevented by unique constraint `uq_appointment_provider_slot`.
  - Concurrent dispensing orders prevented by partial unique index `uq_dispensing_orders_active_prescription`.
  - Tested via multi-threaded concurrency tests (`AppointmentConcurrencyTest`, `DispensingOrderConcurrencyTest`).

### 1.7 API7:2023 - Server-Side Request Forgery (SSRF)
- **Threat**: Malicious callers supplying external webhook URLs for AI inference or notifications.
- **Countermeasure**:
  - External integration URLs (such as `app.ai.service-url`) are strictly loaded from verified application properties/environment variables, never accepted as client parameters.

### 1.8 API8:2023 - Security Misconfiguration
- **Threat**: CORS misconfiguration, verbose debug error pages, or exposed management endpoints.
- **Countermeasure**:
  - CORS strictly configured to whitelist explicit origins (`http://localhost:3000`, `http://localhost:5173`, `http://localhost:8080`).
  - `server.error.include-stacktrace: never` and `include-exception: false` prevent stack trace leaks.
  - Actuator metrics and admin probes require authentication.

### 1.9 API9:2023 - Improper Inventory Management
- **Threat**: Undocumented shadow APIs or deprecated endpoints exposing vulnerabilities.
- **Countermeasure**:
  - Centralized OpenAPI 3.1 documentation exposed via `/v3/api-docs` and `/swagger-ui.html`.
  - Versioned API design under `/api/v1`.

### 1.10 API10:2023 - Unsafe Consumption of APIs
- **Threat**: Crashes or cascading failures when the external AI inference model or external service fails or returns unexpected data.
- **Countermeasure**:
  - Typed DTO validation on all external responses.
  - Resilient fallback mechanism in `AiIntegrationServiceImpl` to prevent system unavailability if the teammate's model server is offline.

---

## 2. Clinical History Protection & Immutability (`ON DELETE RESTRICT`)

In healthcare informatics, deleting clinical history violates medical record compliance standards.
- Every foreign key pointing to `encounters`, `prescriptions`, `diagnoses`, and `vitals` specifies `ON DELETE RESTRICT`.
- Verified in `ClinicalDeletionProtectionTest`: Attempting to delete a prescription that has child prescription items throws `DataIntegrityViolationException` and is blocked by the database engine.
