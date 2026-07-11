package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductDocumentRepository extends MongoRepository<ProductDocument, String> {

    List<ProductDocument> findByCatidAndLocaleOrderByProductidAsc(String catid, String locale);

    Optional<ProductDocument> findByProductidAndLocale(String productid, String locale);
}
