package com.medisimbio.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger Documentation Configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI mediSimbioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediSimbio Backend API")
                        .description("REST API and Modular Monolith backend services for the MediSimbio Healthcare Ecosystem.")
                        .version("v1 (Phase 1 Foundation)")
                        .contact(new Contact()
                                .name("MediSimbio Engineering Team")
                                .url("https://medisimbio.com"))
                        .license(new License()
                                .name("Proprietary - MediSimbio Healthcare")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
