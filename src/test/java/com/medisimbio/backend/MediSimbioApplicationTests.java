package com.medisimbio.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MediSimbioApplicationTests {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Context Loads: Spring Boot application context initializes successfully")
    void contextLoads() {
        // Verification that all Spring beans, JPA repositories, Flyway migrations, and Security configurations initialize without errors
    }
}
