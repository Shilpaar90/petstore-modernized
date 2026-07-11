package com.example.petstore.identity.domain;

/**
 * An application user, as the domain cares about it — the login identity, not the credential.
 * The password hash never appears here; it lives only in the persistence + security adapters
 * (see {@code identity/application/port/out}). Replaces the legacy CMP {@code User} entity.
 */
public record User(String username, boolean enabled) {
}
