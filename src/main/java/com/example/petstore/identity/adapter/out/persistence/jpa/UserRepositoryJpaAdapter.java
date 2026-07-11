package com.example.petstore.identity.adapter.out.persistence.jpa;

import com.example.petstore.identity.application.StoredUser;
import com.example.petstore.identity.application.port.out.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Relational (JPA) implementation of the {@link UserRepository} outbound port.
 */
@Repository
public class UserRepositoryJpaAdapter implements UserRepository {

    private final UserJpaRepository users;

    public UserRepositoryJpaAdapter(UserJpaRepository users) {
        this.users = users;
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.existsById(username);
    }

    @Override
    public void create(String username, String passwordHash, boolean enabled) {
        users.save(new UserEntity(username, passwordHash, enabled));
    }

    @Override
    public Optional<StoredUser> findByUsername(String username) {
        return users.findById(username)
                .map(e -> new StoredUser(e.getUsername(), e.getPassword(), e.isEnabled()));
    }
}
