package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ItemDocumentRepository extends MongoRepository<ItemDocument, String> {

    List<ItemDocument> findByProductidAndLocaleOrderByItemidAsc(String productid, String locale);

    Optional<ItemDocument> findByItemidAndLocale(String itemid, String locale);
}
