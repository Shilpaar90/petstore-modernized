package com.example.petstore.catalog.adapter.out.persistence.mongo;

import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * MongoDB implementation of the {@link CatalogRepository} port (Phase 5 stretch, {@code mongo}
 * profile). Proves the domain and use cases run unchanged against a document store: the same
 * port, a different edge (ADR-0003/0004). Documents are keyed by their real natural id (not a
 * locale partition) with locale resolved by map lookup at read time (ADR-0009); an unsupported
 * locale is treated as "not found", matching the relational adapter's behavior.
 */
@Repository
@Profile("mongo")
public class CatalogRepositoryMongoAdapter implements CatalogRepository {

    private final CategoryDocumentRepository categories;
    private final ProductDocumentRepository products;

    public CatalogRepositoryMongoAdapter(CategoryDocumentRepository categories,
                                         ProductDocumentRepository products) {
        this.categories = categories;
        this.products = products;
    }

    private static String db(Locale locale) {
        return locale.toString();
    }

    @Override
    public List<Category> findCategories(Locale locale) {
        String key = db(locale);
        return categories.findAllByOrderByCatidAsc().stream()
                .filter(d -> d.getI18n().containsKey(key))
                .map(d -> toCategory(d, key))
                .toList();
    }

    @Override
    public Optional<Category> findCategory(String categoryId, Locale locale) {
        String key = db(locale);
        return categories.findById(categoryId)
                .filter(d -> d.getI18n().containsKey(key))
                .map(d -> toCategory(d, key));
    }

    @Override
    public List<Product> findProductsByCategory(String categoryId, Locale locale) {
        String key = db(locale);
        return products.findByCatidOrderByProductidAsc(categoryId).stream()
                .filter(d -> d.getI18n().containsKey(key))
                .map(d -> toProduct(d, key))
                .toList();
    }

    @Override
    public Optional<Product> findProduct(String productId, Locale locale) {
        String key = db(locale);
        return products.findById(productId)
                .filter(d -> d.getI18n().containsKey(key))
                .map(d -> toProduct(d, key));
    }

    @Override
    public List<Item> findItemsByProduct(String productId, Locale locale) {
        String key = db(locale);
        return products.findById(productId)
                .map(d -> d.getItems().stream()
                        .filter(i -> i.getI18n().containsKey(key))
                        .map(i -> toItem(i, d.getProductid(), key))
                        .toList())
                .orElse(List.of());
    }

    @Override
    public Optional<Item> findItem(String itemId, Locale locale) {
        String key = db(locale);
        return products.findByItems_Itemid(itemId)
                .flatMap(d -> d.getItems().stream()
                        .filter(i -> i.getItemid().equals(itemId))
                        .filter(i -> i.getI18n().containsKey(key))
                        .findFirst()
                        .map(i -> toItem(i, d.getProductid(), key)));
    }

    private Category toCategory(CategoryDocument d, String localeKey) {
        LocalizedText t = d.getI18n().get(localeKey);
        return new Category(d.getCatid(), t.name(), d.getImage());
    }

    private Product toProduct(ProductDocument d, String localeKey) {
        LocalizedText t = d.getI18n().get(localeKey);
        return new Product(d.getProductid(), d.getCatid(), t.name(), t.descn(), d.getImage());
    }

    private Item toItem(ProductDocument.ItemEntry i, String productId, String localeKey) {
        ProductDocument.ItemLocale t = i.getI18n().get(localeKey);
        return new Item(i.getItemid(), productId, t.getListprice(), t.getUnitcost(),
                i.getImage(), t.getDescn(), t.getAttributes());
    }
}
