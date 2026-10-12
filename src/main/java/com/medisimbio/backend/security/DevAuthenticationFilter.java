package com.medisimbio.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Development-Mode Authentication Filter.
 * 
 * =============================================================================
 * STATUS: DEVELOPMENT / LOCAL / TEST ONLY (Phase 1-4 Hardening)
 * =============================================================================
 * Allows seamless local development, testing, and integration by simulating
 * authenticated user context from headers (e.g. Authorization: Bearer dev-user-123
 * or X-User-Id: user_123).
 * 
 * CRITICAL SAFETY BOUNDARY:
 * This filter is strictly constrained to "local", "test", "dev", and "default" profiles.
 * In a "production" profile, this bean is NOT loaded, preventing header-based or
 * arbitrary Bearer authentication from acting as an identity source.
 * In production deployment, standard OAuth2 / OpenID Connect JWT token validation
 * (e.g., Spring Security OAuth2 Resource Server / Keycloak) will populate the
 * SecurityContext.
 */
@Component
@Profile({"local", "test", "dev", "default"})
public class DevAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DevAuthenticationFilter.class);

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if authentication is already present in context
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = null;
            String userEmail = request.getHeader(HEADER_USER_EMAIL);

            // 1. Check X-User-Id header
            String customUserId = request.getHeader(HEADER_USER_ID);
            if (customUserId != null && !customUserId.isBlank()) {
                userId = customUserId.trim();
            }

            // 2. Check Authorization Bearer header
            String authHeader = request.getHeader(HEADER_AUTHORIZATION);
            if (userId == null && authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length()).trim();
                if (!token.isBlank()) {
                    userId = token;
                }
            }

            // If an identity was extracted, establish security context
            if (userId != null) {
                if (userEmail == null || userEmail.isBlank()) {
                    userEmail = userId + "@medisimbio.local";
                }

                UserPrincipal principal = new UserPrincipal(
                        userId,
                        userId,
                        userEmail,
                        Set.of(new SimpleGrantedAuthority("ROLE_" + SecurityConstants.ROLE_PATIENT)),
                        true
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Dev authentication established for user ID [{}] on [{}]", userId, request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}
