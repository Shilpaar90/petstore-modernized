package com.example.petstore.order.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderDocumentRepository extends MongoRepository<OrderDocument, String> {

    List<OrderDocument> findByUsernameOrderByPlacedAtDesc(String username);
}
