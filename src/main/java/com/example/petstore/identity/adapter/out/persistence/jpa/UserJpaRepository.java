package com.example.petstore.identity.adapter.out.persistence.jpa;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserJpaRepository extends Repository<UserEntity, String> {

    boolean existsById(String username);

    Optional<UserEntity> findById(String username);

    UserEntity save(UserEntity user);
}
