package com.medisimbio.backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * UserPrincipal representing an authenticated identity in the security context.
 * 
 * NOTE: FOUNDATION ONLY / NOT PRODUCTION READY (Phase 1).
 * In future phases, this will be populated from verified JWT / OIDC claims.
 */
public class UserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String email;
    private final Set<GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(String id, String username, String email, Set<GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.authorities = authorities != null ? Collections.unmodifiableSet(authorities) : Collections.emptySet();
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return ""; // Passwords are not stored in memory for stateless JWT architecture
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
