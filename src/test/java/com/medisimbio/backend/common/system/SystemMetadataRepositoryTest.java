package com.medisimbio.backend.common.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SystemMetadataRepositoryTest {

    @Autowired
    private SystemMetadataRepository systemMetadataRepository;

    @Test
    @DisplayName("Flyway Initial Migration: Seed metadata record exists in database")
    void flywayMigration_shouldHaveSeededBootstrapRecord() {
        Optional<SystemMetadataEntity> record = systemMetadataRepository.findByConfigKey("PLATFORM_PHASE");
        assertThat(record).isPresent();
        assertThat(record.get().getConfigValue()).isEqualTo("PHASE_1_FOUNDATION");
        assertThat(record.get().isActive()).isTrue();
        assertThat(record.get().getCreatedAt()).isNotNull();
        assertThat(record.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("JPA CRUD & Auditing: Can persist and query new system metadata entity")
    void jpaOperations_shouldPersistAndRetrieveEntity() {
        SystemMetadataEntity newEntity = new SystemMetadataEntity(
                "sys_test_002",
                "TEST_SETTING_KEY",
                "TEST_VALUE_123",
                "Unit testing verification key",
                true
        );

        SystemMetadataEntity saved = systemMetadataRepository.save(newEntity);
        assertThat(saved.getId()).isEqualTo("sys_test_002");
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<SystemMetadataEntity> retrieved = systemMetadataRepository.findById("sys_test_002");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getConfigKey()).isEqualTo("TEST_SETTING_KEY");
        assertThat(retrieved.get().getConfigValue()).isEqualTo("TEST_VALUE_123");
    }
}
