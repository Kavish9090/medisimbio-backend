package com.medisimbio.backend.common.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for System Metadata verification entity.
 */
@Repository
public interface SystemMetadataRepository extends JpaRepository<SystemMetadataEntity, String> {

    Optional<SystemMetadataEntity> findByConfigKey(String configKey);

    boolean existsByConfigKey(String configKey);
}
