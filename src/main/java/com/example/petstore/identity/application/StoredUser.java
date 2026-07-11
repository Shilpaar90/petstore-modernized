package com.example.petstore.identity.application;

/**
 * Outbound-port DTO carrying the persisted credential — the username, the (already-hashed)
 * password, and the enabled flag. Used by the security adapter to build Spring Security's
 * {@code UserDetails}; deliberately separate from the framework-free {@code User} domain record.
 */
public record StoredUser(String username, String passwordHash, boolean enabled) {
}
