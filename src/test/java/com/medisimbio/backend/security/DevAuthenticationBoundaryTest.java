package com.medisimbio.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DevAuthenticationBoundaryTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Development / Test Profile Authentication Boundary")
    class TestProfileTests {

        @Autowired
        private Optional<DevAuthenticationFilter> devAuthenticationFilterOptional;

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Test profile: DevAuthenticationFilter bean is instantiated and registered")
        void shouldInstantiateDevFilterInTestProfile() {
            assertTrue(devAuthenticationFilterOptional.isPresent(),
                    "DevAuthenticationFilter must be loaded in test/local profiles for automated test execution");
        }

        @Test
        @DisplayName("Test profile: Public endpoints are accessible without authentication")
        void shouldAllowAccessToPublicEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("production")
    @org.springframework.test.context.TestPropertySource(properties = {
            "spring.datasource.url=jdbc:h2:mem:prod_sec_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/h2"
    })
    @DisplayName("Production Profile Security Boundary")
    class ProductionProfileTests {

        @Autowired
        private Optional<DevAuthenticationFilter> devAuthenticationFilterOptional;

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("Production profile: DevAuthenticationFilter bean is NOT instantiated")
        void shouldNotInstantiateDevFilterInProductionProfile() {
            assertFalse(devAuthenticationFilterOptional.isPresent(),
                    "DevAuthenticationFilter must NEVER be loaded in production profile");
        }

        @Test
        @DisplayName("Production profile: X-User-Id header cannot authenticate requests (returns 401)")
        void shouldRejectXUserIdHeaderInProduction() throws Exception {
            mockMvc.perform(get("/api/v1/patients/me")
                            .header(DevAuthenticationFilter.HEADER_USER_ID, "attacker_mock_user_123"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Production profile: Arbitrary Bearer header cannot authenticate requests (returns 401)")
        void shouldRejectArbitraryBearerTokenInProduction() throws Exception {
            mockMvc.perform(get("/api/v1/patients/me")
                            .header("Authorization", "Bearer fake_dev_jwt_or_user_id"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Production profile: Public endpoints remain accessible")
        void shouldAllowPublicEndpointsInProduction() throws Exception {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk());
        }
    }
}
