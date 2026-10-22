# MediSimbio Backend: Windows Local Developer Setup Guide

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\LOCAL_SETUP_WINDOWS.md`  
**Date**: October 2026  

---

## 1. Prerequisites

Before starting, ensure your Windows machine has:
1. **Java JDK 21** installed (e.g., Eclipse Temurin, Microsoft OpenJDK, or Amazon Corretto).
   Verify in PowerShell:
   ```powershell
   java -version
   ```
   *Expected output: `openjdk version "21.x.x"`*
2. **Git for Windows** (with Git Bash or PowerShell).
3. **Docker Desktop for Windows** (Recommended for running PostgreSQL 16 locally).

---

## 2. PostgreSQL 16 Setup (Docker Compose)

The project includes an optimized `docker-compose.yml` for PostgreSQL 16 on port **5433** (to prevent conflicts with any local PostgreSQL instance installed on default port 5432).

### 2.1 Starting the Database:
Open PowerShell in `D:\medsimbio\medisimbio-backend`:
```powershell
docker compose up -d
```
Verify container status:
```powershell
docker ps --filter "name=medisimbio-postgres"
```

### 2.2 Default Local Credentials:
- **Host**: `localhost`
- **Port**: `5433`
- **Database**: `medisimbio_db`
- **Username**: `medisimbio_user`
- **Password**: `medisimbio_pass`

### 2.3 Resolving PostgreSQL Password Mismatch (`SQLSTATE 28P01`):
If you previously initialized the Docker container with an old password and encounter:
`FATAL: password authentication failed for user "medisimbio_user" (SQLSTATE 28P01)`
**Do NOT delete your Docker volume**. Instead, sync the password inside the container:
```powershell
docker exec -it medisimbio-postgres psql -U postgres -c "ALTER USER medisimbio_user WITH PASSWORD 'medisimbio_pass';"
```

---

## 3. Running & Testing the Backend

### 3.1 Build and Package:
```powershell
.\gradlew assemble
```
Produces the runnable executable JAR at `build/libs/medisimbio-backend-0.0.1-SNAPSHOT.jar`.

### 3.2 Run the Application:
```powershell
.\gradlew bootRun
```
*The server starts on `http://localhost:8081`.*

### 3.3 Run Automated Tests:
```powershell
.\gradlew test
```
Executes the complete 150-test automated suite across unit, controller, multi-threaded concurrency, and integrity checks.

---

## 4. Developer Tools & Verification

### 4.1 Interactive Swagger UI:
Open your browser to:
`http://localhost:8081/swagger-ui.html`

Click the **Authorize** button in Swagger to input local dev credentials:
- `devUserIdHeader`: `auth_pat_1` (or `auth_doc_1`)
- `devUserRoleHeader`: `PATIENT` (or `DOCTOR`)

### 4.2 Actuator Health Check:
```powershell
curl http://localhost:8081/actuator/health
```
*Expected response: `{"status":"UP","components":{...}}`*
