package com.medisimbio.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA Auditing for timestamp generation on BaseEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
