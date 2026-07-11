package com.example.petstore.catalog.application.port.in;

import com.example.petstore.catalog.application.CategoryPage;
import com.example.petstore.catalog.application.ProductPage;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Inbound (driving) port for read-only catalog browsing — the use-case API the web adapters
 * depend on, rather than reaching for the outbound {@code CatalogRepository} directly. Keeping a
 * distinct inbound port is what lets controllers be tested as slices (the port is mocked) and
 * keeps navigation/composition logic in one place (ADR-0003).
 *
 * <p>Lookups that can miss return {@link Optional} so the web layer can map absence to HTTP 404
 * without the application throwing.
 */
public interface CatalogQuery {

    /** All top-level categories for the storefront landing/browse page. */
    List<Category> listCategories(Locale locale);

    /** A category together with its products; empty if the category id is unknown for this locale. */
    Optional<CategoryPage> viewCategory(String categoryId, Locale locale);

    /** A product together with its purchasable items; empty if the product id is unknown. */
    Optional<ProductPage> viewProduct(String productId, Locale locale);

    /** A single item's detail; empty if the item id is unknown. */
    Optional<Item> viewItem(String itemId, Locale locale);
}
