# MediSimbio Backend: Production Operations, Observability & Reliability Guide

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\PRODUCTION_OPERATIONS.md`  
**Date**: October 2026  

---

## 1. Observability, Probes & Metrics

### 1.1 Actuator Health & Probes
In production, Spring Boot exposes standard Kubernetes-compatible liveness and readiness probes:
- **Liveness Probe**: `http://localhost:8081/actuator/health/liveness` (Returns `UP` if process is healthy)
- **Readiness Probe**: `http://localhost:8081/actuator/health/readiness` (Returns `UP` when HikariCP pool and Flyway migrations are active)

### 1.2 Metrics & Prometheus Scrape
- Metrics are exposed under `/actuator/metrics`.
- Key metrics to monitor:
  - `hikaricp.connections.active`: Currently busy database connections.
  - `hikaricp.connections.pending`: Threads waiting for a database connection (should remain near 0).
  - `http.server.requests`: Request latency percentiles (P95, P99) and status code distributions (2xx, 4xx, 5xx).
  - `jvm.memory.used`: Heap and non-heap memory utilization.

---

## 2. Connection Pooling & Database Tuning (HikariCP)

In `application-production.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1800000
      pool-name: MediSimbioHikariProdPool
```
- **Connection Timeout**: Set to 20 seconds. Prevents request threads from hanging indefinitely during database contention.
- **Max Lifetime**: Refreshes connections every 30 minutes to mitigate silent connection drops across stateful cloud firewalls.

---

## 3. Production Logging & Data Privacy Standards

1. **Zero Clinical PII in Application Logs**:
   - Patient names, Med-IDs, phone numbers, and clinical diagnoses must never be logged at INFO or WARN levels.
   - Only non-PII operational identifiers (e.g. `UUID` of encounter, status enum transitions, HTTP status codes) are emitted:
     ```
     2026-10-22 03:45:12.100 [main] INFO  c.m.b.e.s.i.EncounterServiceImpl - Transitioning encounter [c70c0000-...] to IN_PROGRESS by doctor [auth_doc_1]
     ```
2. **Production Log Suppression**:
   - Hibernate SQL formatting and parameter logging are disabled (`show-sql: false`, `format_sql: false`).
   - Root logger level is set to `WARN`; application package set to `INFO`.

---

## 4. Graceful Shutdown & Disaster Recovery

### 4.1 Graceful Shutdown
Spring Boot is configured for graceful termination:
```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```
When `SIGTERM` is received, the container stops accepting new traffic, allows in-flight HTTP requests up to 30 seconds to finish, closes open database transactions, and disconnects HikariCP cleanly.

### 4.2 Database Backup & Restoration Runbook
- **Daily Full Backup**:
  ```bash
  pg_dump -h <pg-host> -U medisimbio_user -d medisimbio_db -Fc -f /backups/medisimbio_$(date +%Y%m%d).dump
  ```
- **Disaster Restoration Procedure**:
  ```bash
  pg_restore -h <pg-host> -U medisimbio_user -d medisimbio_db -c /backups/medisimbio_20261022.dump
  ```
- **Recovery Verification Frequency**: Quarterly automated restoration tests on staging to verify backup validity.
