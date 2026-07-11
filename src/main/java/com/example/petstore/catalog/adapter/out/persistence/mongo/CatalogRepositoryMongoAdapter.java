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
 * port, a different edge (ADR-0003/0004). Locale is the legacy underscore form ({@code en_US}).
 */
@Repository
@Profile("mongo")
public class CatalogRepositoryMongoAdapter implements CatalogRepository {

    private final CategoryDocumentRepository categories;
    private final ProductDocumentRepository products;
    private final ItemDocumentRepository items;

    public CatalogRepositoryMongoAdapter(CategoryDocumentRepository categories,
                                         ProductDocumentRepository products,
                                         ItemDocumentRepository items) {
        this.categories = categories;
        this.products = products;
        this.items = items;
    }

    private static String db(Locale locale) {
        return locale.toString();
    }

    @Override
    public List<Category> findCategories(Locale locale) {
        return categories.findByLocaleOrderByCatidAsc(db(locale)).stream().map(this::toCategory).toList();
    }

    @Override
    public Optional<Category> findCategory(String categoryId, Locale locale) {
        return categories.findByCatidAndLocale(categoryId, db(locale)).map(this::toCategory);
    }

    @Override
    public List<Product> findProductsByCategory(String categoryId, Locale locale) {
        return products.findByCatidAndLocaleOrderByProductidAsc(categoryId, db(locale)).stream()
                .map(this::toProduct).toList();
    }

    @Override
    public Optional<Product> findProduct(String productId, Locale locale) {
        return products.findByProductidAndLocale(productId, db(locale)).map(this::toProduct);
    }

    @Override
    public List<Item> findItemsByProduct(String productId, Locale locale) {
        return items.findByProductidAndLocaleOrderByItemidAsc(productId, db(locale)).stream()
                .map(this::toItem).toList();
    }

    @Override
    public Optional<Item> findItem(String itemId, Locale locale) {
        return items.findByItemidAndLocale(itemId, db(locale)).map(this::toItem);
    }

    private Category toCategory(CategoryDocument d) {
        return new Category(d.getCatid(), d.getName(), d.getImage());
    }

    private Product toProduct(ProductDocument d) {
        return new Product(d.getProductid(), d.getCatid(), d.getName(), d.getDescn(), d.getImage());
    }

    private Item toItem(ItemDocument d) {
        return new Item(d.getItemid(), d.getProductid(), d.getListprice(), d.getUnitcost(),
                d.getImage(), d.getDescn(), d.getAttributes());
    }
}
