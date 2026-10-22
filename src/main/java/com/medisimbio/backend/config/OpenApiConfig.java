package com.medisimbio.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger Documentation Configuration for MediSimbio Integration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI mediSimbioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediSimbio Healthcare Ecosystem Backend API")
                        .description("Modular monolith REST API supporting Web Portals, Flutter Mobile, and AI Inference Service. "
                                + "Covers Patient & Med-ID management, Doctor/Hospital catalog, Appointments, Encounters, "
                                + "Vitals, Diagnoses, E-Prescriptions, Medicine Dispensing Orders, and Medical Timeline.")
                        .version("1.0.0 (Phase 1-6 Integration Complete)")
                        .contact(new Contact()
                                .name("MediSimbio Architecture Team")
                                .url("https://medisimbio.com"))
                        .license(new License()
                                .name("Proprietary - MediSimbio Healthcare")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Production JWT Bearer Token"))
                        .addSecuritySchemes("devUserIdHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("Dev/Test User ID header (e.g., auth_pat_1, auth_doc_1)"))
                        .addSecuritySchemes("devUserRoleHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Role")
                                .description("Dev/Test User Role header (e.g., PATIENT, DOCTOR, HOSPITAL_ADMIN, PHARMACY)")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                        .addList("devUserIdHeader")
                        .addList("devUserRoleHeader"));
    }
}
