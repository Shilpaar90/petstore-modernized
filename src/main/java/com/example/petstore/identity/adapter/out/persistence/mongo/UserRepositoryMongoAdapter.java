package com.example.petstore.identity.adapter.out.persistence.mongo;

import com.example.petstore.identity.application.StoredUser;
import com.example.petstore.identity.application.port.out.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB implementation of the {@link UserRepository} port ({@code mongo} profile). Same port,
 * different store — Spring Security and the registration use case are unaffected.
 */
@Repository
@Profile("mongo")
public class UserRepositoryMongoAdapter implements UserRepository {

    private final UserDocumentRepository users;

    public UserRepositoryMongoAdapter(UserDocumentRepository users) {
        this.users = users;
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.existsById(username);
    }

    @Override
    public void create(String username, String passwordHash, boolean enabled) {
        users.save(new UserDocument(username, passwordHash, enabled));
    }

    @Override
    public Optional<StoredUser> findByUsername(String username) {
        return users.findById(username)
                .map(d -> new StoredUser(d.getUsername(), d.getPassword(), d.isEnabled()));
    }
}
