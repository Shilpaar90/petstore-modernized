package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductDocumentRepository extends MongoRepository<ProductDocument, String> {

    List<ProductDocument> findByCatidOrderByProductidAsc(String catid);

    /** Finds the owning product for an item id — items are embedded, so this queries into the
     *  {@code items.itemid} array field rather than a standalone item collection (ADR-0009). */
    Optional<ProductDocument> findByItems_Itemid(String itemid);
}
