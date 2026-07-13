package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryDocumentRepository extends MongoRepository<CategoryDocument, String> {

    List<CategoryDocument> findAllByOrderByCatidAsc();
}
