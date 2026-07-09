package com.example.petstore.catalog.application.port;

import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Outbound port for read-only catalog access. The application depends on this interface; the
 * relational (JPA) and MongoDB adapters implement it (ADR-0003/0004). All lookups are
 * locale-scoped, faithfully reflecting the legacy localized {@code *_details} model.
 */
public interface CatalogRepository {

    List<Category> findCategories(Locale locale);

    Optional<Category> findCategory(String categoryId, Locale locale);

    List<Product> findProductsByCategory(String categoryId, Locale locale);

    Optional<Product> findProduct(String productId, Locale locale);

    List<Item> findItemsByProduct(String productId, Locale locale);

    Optional<Item> findItem(String itemId, Locale locale);
}
