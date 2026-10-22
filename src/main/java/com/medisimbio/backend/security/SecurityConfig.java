package com.medisimbio.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Foundation Configuration.
 * 
 * =============================================================================
 * STATUS: FOUNDATION ONLY / NOT PRODUCTION READY (Phase 2)
 * =============================================================================
 * - Stateless REST API session policy.
 * - CSRF disabled for stateless REST endpoints.
 * - Standardized JSON 401/403 exception handling via entry points.
 * - Public whitelist for health, actuator, and OpenAPI/Swagger documentation.
 * - DevAuthenticationFilter establishes mock/header auth in dev/local environments.
 * - Secure baseline where any non-whitelisted endpoint requires authentication.
 */
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final Optional<DevAuthenticationFilter> devAuthenticationFilterOptional;

    public SecurityConfig(RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          Optional<DevAuthenticationFilter> devAuthenticationFilterOptional) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.devAuthenticationFilterOptional = devAuthenticationFilterOptional;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Enable standard CORS handling
                .cors(Customizer.withDefaults())

                // Stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Standardized REST exception handling for unauthorized / forbidden requests
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Endpoint authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.PUBLIC_URL_PATTERNS).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/specialties/**",
                                "/api/v1/hospitals",
                                "/api/v1/hospitals/{id}",
                                "/api/v1/hospitals/{id}/providers",
                                "/api/v1/providers",
                                "/api/v1/providers/{id}",
                                "/api/v1/providers/{id}/specialties",
                                "/api/v1/providers/{id}/hospitals",
                                "/api/v1/providers/{providerId}/availability",
                                "/api/v1/providers/{providerId}/availability/**",
                                "/api/v1/providers/{providerId}/slots"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        // Register DevAuthenticationFilter ONLY if the bean is present (local/test/dev profiles)
        devAuthenticationFilterOptional.ifPresent(filter ->
                http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(
            @org.springframework.beans.factory.annotation.Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://localhost:5173}") String allowedOrigins) {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        java.util.List<String> origins = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setExposedHeaders(java.util.List.of("Authorization", "Content-Disposition", "Location", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
