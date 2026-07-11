package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryDocumentRepository extends MongoRepository<CategoryDocument, String> {

    List<CategoryDocument> findByLocaleOrderByCatidAsc(String locale);

    Optional<CategoryDocument> findByCatidAndLocale(String catid, String locale);
}
