package com.example.petstore.identity.application.port.out;

import com.example.petstore.identity.application.StoredUser;

import java.util.Optional;

/**
 * Outbound port for user persistence. The JPA adapter implements it today; a MongoDB adapter can
 * implement it later (Phase 5) without the application changing (ADR-0003/0004).
 */
public interface UserRepository {

    boolean existsByUsername(String username);

    /** Persists a new user with an already-hashed password. */
    void create(String username, String passwordHash, boolean enabled);

    Optional<StoredUser> findByUsername(String username);
}
