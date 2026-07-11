package com.example.petstore.identity.application.port.out;

/**
 * Outbound port for one-way password hashing. Keeps the registration use case free of any
 * security framework; the adapter wraps Spring Security's {@code PasswordEncoder} (BCrypt,
 * ADR-0005). Verification at login is Spring Security's concern, not the application's.
 */
public interface PasswordHasher {

    String hash(String rawPassword);
}
