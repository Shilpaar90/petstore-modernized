package com.example.petstore.identity.application.port.in;

import com.example.petstore.identity.domain.User;

/**
 * Inbound port for the self-service registration use case. Replaces the legacy account-creation
 * screen flow.
 */
public interface RegisterUser {

    /**
     * Registers a new user.
     *
     * @throws com.example.petstore.identity.application.UsernameTakenException if the username exists
     * @throws IllegalArgumentException                                         if username/password is blank
     */
    User register(String username, String rawPassword);
}
