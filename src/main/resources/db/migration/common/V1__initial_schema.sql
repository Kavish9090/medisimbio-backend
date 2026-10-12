-- =============================================================================
-- MediSimbio Database Migration: V1 Initial Foundation Schema
-- Description: Minimal system foundation table to verify Spring Boot, JPA, 
--              Flyway, and PostgreSQL integration without prematurely adding 
--              unverified domain tables.
-- =============================================================================

CREATE TABLE system_metadata (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for key lookups
CREATE INDEX idx_system_metadata_key ON system_metadata(config_key);

-- Initial Bootstrap Verification Record
INSERT INTO system_metadata (id, config_key, config_value, description, is_active, created_at, updated_at)
VALUES (
    'sys_bootstrap_001',
    'PLATFORM_PHASE',
    'PHASE_1_FOUNDATION',
    'MediSimbio backend foundation initialization verification marker',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
