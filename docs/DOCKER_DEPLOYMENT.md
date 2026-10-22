# MediSimbio Backend: Docker Deployment & Container Orchestration

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\DOCKER_DEPLOYMENT.md`  
**Date**: October 2026  

---

## 1. Multi-Stage Dockerfile Specification

For production deployments, the application uses an optimized, multi-stage Alpine-based container build producing a lightweight non-root runtime image:

```dockerfile
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S medisimbio && adduser -S medisimbio -G medisimbio
USER medisimbio:medisimbio

COPY --from=builder /workspace/build/libs/medisimbio-backend-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=production \
    JAVA_TOOL_OPTIONS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0"

EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 2. Docker Compose Infrastructure

The repository provides `docker-compose.yml` for isolated containerized database hosting:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: medisimbio-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${DB_NAME:-medisimbio_db}
      POSTGRES_USER: ${DB_USERNAME:-medisimbio_user}
      POSTGRES_PASSWORD: ${DB_PASSWORD:?Set DB_PASSWORD in your .env file}
    ports:
      - "${DB_PORT:-5433}:5432"
    volumes:
      - medisimbio_postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  medisimbio_postgres_data:
    name: medisimbio_postgres_data
```

---

## 3. Operational Container Commands

| Operation | Command | Purpose |
|---|---|---|
| **Start Database** | `docker compose up -d` | Launches PostgreSQL 16 container in background |
| **Check Health** | `docker compose ps` | Verifies container status and `(healthy)` indicator |
| **View Logs** | `docker compose logs -f postgres` | Follows PostgreSQL database log stream |
| **Stop Database** | `docker compose stop` | Gracefully shuts down PostgreSQL without data loss |
| **Backup Database** | `docker exec -t medisimbio-postgres pg_dump -U medisimbio_user medisimbio_db > backup.sql` | Exports complete SQL database backup |
| **Sync Password** | `docker exec -it medisimbio-postgres psql -U postgres -c "ALTER USER medisimbio_user WITH PASSWORD '...';"` | Non-destructive password sync |
