package com.medisimbio.backend.security;

import java.util.Optional;

/**
 * Authentication Facade abstraction for extracting the current authenticated user identity.
 * Decouples domain services from Spring Security context specifics.
 */
public interface AuthenticationFacade {

    /**
     * Gets the authenticated user's unique identity (subject/UID).
     *
     * @return Optional containing the authenticated user's ID
     */
    Optional<String> getCurrentUserId();

    /**
     * Gets the authenticated user's unique identity or throws UnauthorizedException.
     *
     * @return The authenticated user's ID
     */
    String getRequiredCurrentUserId();

    /**
     * Gets the authenticated user's email if present.
     *
     * @return Optional containing email
     */
    Optional<String> getCurrentUserEmail();

    /**
     * Checks if a user is currently authenticated in the request context.
     *
     * @return true if authenticated
     */
    boolean isAuthenticated();
}
