# MediSimbio Backend: Authentication Architecture & Security Boundaries

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\AUTHENTICATION.md`  
**Date**: October 2026  

---

## 1. Authentication Architecture Overview

The MediSimbio backend operates as a **stateless REST API**. Session creation is strictly disabled:
```java
http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```
Every incoming request must carry credentials in its headers. The system extracts the caller's unique subject identity and authorities and populates Spring's `SecurityContext`.

---

## 2. Development & Integration Mode (`DevAuthenticationFilter`)

To enable immediate, friction-free local development and integration testing across Web Portals, Mobile, and automated suites without requiring a live external OAuth2/OIDC server:

### 2.1 Profile Constraints (Fail-Closed)
`DevAuthenticationFilter` is strictly annotated:
```java
@Component
@Profile({"local", "test", "dev", "default"})
public class DevAuthenticationFilter extends OncePerRequestFilter { ... }
```
In any environment running with the `production` profile, **this bean is not instantiated by Spring**.

### 2.2 Accepted Headers in Development
In non-production profiles, `DevAuthenticationFilter` accepts:
1. `X-User-Id: <userId>` (e.g. `auth_pat_1` or `auth_doc_1`)
2. `Authorization: Bearer <userId>`
3. `X-User-Role: <ROLE>` (e.g. `DOCTOR`, `PATIENT`, `HOSPITAL_ADMIN`, `PHARMACY`)
4. `X-User-Roles: <ROLE1>,<ROLE2>` (comma-separated roles)
5. `X-User-Email: <email>` (optional user email)

### 2.3 Automated Proof of Production Safety
The test suite in [`DevAuthenticationBoundaryTest.java`](file:///D:/medsimbio/medisimbio-backend/src/test/java/com/medisimbio/backend/security/DevAuthenticationBoundaryTest.java) executes against `@ActiveProfiles("production")` and proves:
- `DevAuthenticationFilter` bean is absent (`assertFalse(devAuthenticationFilterOptional.isPresent())`).
- Sending `X-User-Id` in production returns **HTTP 401 Unauthorized**.
- Sending `Authorization: Bearer <arbitrary>` in production returns **HTTP 401 Unauthorized**.
- Sending `X-User-Role` in production returns **HTTP 401 Unauthorized**.

---

## 3. Production Authentication Roadmap (OAuth2 / OIDC)

In production deployment:
1. **Identity Provider**: An OIDC authority (e.g. Keycloak, AWS Cognito, or Firebase Auth) issues cryptographically signed JWT tokens (RS256).
2. **Spring Security Resource Server**:
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: https://auth.medisimbio.com/realms/medisimbio
             jwk-set-uri: https://auth.medisimbio.com/realms/medisimbio/protocol/openid-connect/certs
   ```
3. **Identity Extraction**:
   The `SecurityContextAuthenticationFacade` will continue to extract `principal.getId()` (mapped from the JWT `sub` claim) seamlessly, requiring **zero changes** to downstream clinical business logic.

---

## 4. Standardized JSON Error Handling

When authentication or authorization fails, Spring Security delegates to custom handlers returning uniform JSON rather than HTML redirects:

- **Unauthenticated (HTTP 401 Unauthorized)**: Handled by `RestAuthenticationEntryPoint`:
  ```json
  {
    "timestamp": "2026-10-22T03:45:00Z",
    "status": 401,
    "error": "UNAUTHORIZED",
    "message": "Full authentication is required to access this resource",
    "path": "/api/v1/patients/me"
  }
  ```
- **Unauthorized / Forbidden (HTTP 403 Forbidden)**: Handled by `RestAccessDeniedHandler`:
  ```json
  {
    "timestamp": "2026-10-22T03:45:00Z",
    "status": 403,
    "error": "FORBIDDEN",
    "message": "Access is denied for this resource",
    "path": "/api/v1/prescriptions/rx-123/finalize"
  }
  ```
