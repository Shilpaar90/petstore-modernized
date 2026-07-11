package com.example.petstore.identity.application;

/**
 * Raised when registration is attempted with a username that already exists. The web adapter
 * maps this to a friendly form error rather than a 500.
 */
public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String username) {
        super("Username already taken: " + username);
    }
}
