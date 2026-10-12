# MediSimbio Backend Development Setup Guide

## 1. Prerequisites

Before running the backend, ensure your development machine has the following tools installed:

| Tool | Recommended Version | Purpose |
| :--- | :--- | :--- |
| **Java JDK** | **Java 21 LTS** (Temurin / OpenJDK) | Backend Runtime & Compiler |
| **Docker & Docker Compose** | Docker Desktop 24+ / Engine 24+ | Local PostgreSQL 16 Container |
| **Gradle** | Wrapper included (`gradlew` / `gradlew.bat`) | Build & Dependency Tool |
| **cURL / Postman / Browser** | Any modern version | API Testing |

Verify your environment:
```powershell
java -version
docker --version
```

---

## 2. Quick Start (Step-by-Step)

### Step 1: Navigate to Backend Directory
```powershell
cd D:\medsimbio\medisimbio-backend
```

### Step 2: Configure Environment Variables
Create your local `.env` file from the provided template:
```powershell
Copy-Item .env.example .env
```

Review or customize environment variables in `.env`:
```env
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=local

DB_HOST=localhost
DB_PORT=5432
DB_NAME=medisimbio_db
DB_USERNAME=medisimbio_user
DB_PASSWORD=medisimbio_secure_password
```

> **Note**: If port `5432` is already in use by another local service on your machine, you can change `DB_PORT=5433` in `.env`.

---

### Step 3: Start PostgreSQL Container
Start PostgreSQL using Docker Compose:
```powershell
docker compose up -d
```

Verify the database container is running and healthy:
```powershell
docker compose ps
```

---

### Step 4: Run Flyway Migrations & Start Backend
Launch the Spring Boot backend using the Gradle wrapper:

**On Windows (PowerShell):**
```powershell
.\gradlew.bat bootRun
```

**On Linux / macOS:**
```bash
./gradlew bootRun
```

On startup, Flyway will automatically execute migrations from `src/main/resources/db/migration/` and verify table integrity.

---

## 3. Running Automated Tests

Run the full automated test suite (including context loading, health endpoint tests, global error handling tests, and repository tests with Flyway migrations):

```powershell
.\gradlew.bat test
```

Generate a full build and executable JAR:
```powershell
.\gradlew.bat build
```

Test reports are generated at:
`build/reports/tests/test/index.html`

---

## 4. Verifying Endpoints

Once the application is running, test the following endpoints in your browser or terminal:

### 4.1 Health Endpoint
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/health" -Method Get
```
Expected output:
```json
{
  "service": "medisimbio-backend",
  "status": "UP",
  "version": "v1",
  "timestamp": "2026-10-05T10:39:05.151Z"
}
```

### 4.2 Spring Boot Actuator Health
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -Method Get
```

### 4.3 Interactive Swagger / OpenAPI Documentation
Open your browser and navigate to:
```
http://localhost:8081/swagger-ui.html
```
Or view the raw OpenAPI 3 JSON specification:
```
http://localhost:8081/v3/api-docs
```

---

## 5. Stopping Services

To stop the Spring Boot application: Press `Ctrl + C` in the terminal.

To stop the local PostgreSQL container:
```powershell
docker compose stop
```

To stop and remove containers (preserving data in docker volume):
```powershell
docker compose down
```

To stop and wipe database volume completely:
```powershell
docker compose down -v
```
