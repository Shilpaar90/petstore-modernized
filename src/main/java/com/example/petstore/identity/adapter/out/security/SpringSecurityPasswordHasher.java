package com.example.petstore.identity.adapter.out.security;

import com.example.petstore.identity.application.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapts the {@link PasswordHasher} outbound port to Spring Security's {@link PasswordEncoder}
 * (BCrypt bean from {@code SecurityConfig}). This is the only place the application's hashing
 * touches the security framework.
 */
@Component
public class SpringSecurityPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public SpringSecurityPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
