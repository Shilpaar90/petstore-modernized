package com.example.petstore.identity.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {
}
