package com.example.petstore.identity.application;

import com.example.petstore.identity.application.port.in.RegisterUser;
import com.example.petstore.identity.application.port.out.PasswordHasher;
import com.example.petstore.identity.application.port.out.UserRepository;
import com.example.petstore.identity.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The registration use case: validate, reject duplicates, hash the password via the outbound
 * {@link PasswordHasher} port, and persist. Framework-free apart from the Spring stereotype and
 * transaction boundary.
 */
@Service
public class RegistrationService implements RegisterUser {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public RegistrationService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public User register(String username, String rawPassword) {
        String name = username == null ? "" : username.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (users.existsByUsername(name)) {
            throw new UsernameTakenException(name);
        }
        users.create(name, passwordHasher.hash(rawPassword), true);
        return new User(name, true);
    }
}
