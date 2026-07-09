package com.example.petstore.catalog.adapter.out.persistence.jpa;

import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.CategoryDetailEntity;
import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.ItemDetailEntity;
import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.ProductDetailEntity;
import com.example.petstore.catalog.adapter.out.persistence.jpa.repository.CategoryDetailJpaRepository;
import com.example.petstore.catalog.adapter.out.persistence.jpa.repository.ItemDetailJpaRepository;
import com.example.petstore.catalog.adapter.out.persistence.jpa.repository.ProductDetailJpaRepository;
import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Relational (JPA) implementation of the {@link CatalogRepository} port — the faithful migration
 * of the legacy Cloudscape/Oracle catalog DAOs (ADR-0004).
 *
 * <p>Methods are read-only transactional so the lazy {@code product}/{@code item} associations
 * used during mapping resolve within an open session.
 */
@Repository
@Transactional(readOnly = true)
public class CatalogRepositoryJpaAdapter implements CatalogRepository {

    private final CategoryDetailJpaRepository categories;
    private final ProductDetailJpaRepository products;
    private final ItemDetailJpaRepository items;

    public CatalogRepositoryJpaAdapter(CategoryDetailJpaRepository categories,
                                       ProductDetailJpaRepository products,
                                       ItemDetailJpaRepository items) {
        this.categories = categories;
        this.products = products;
        this.items = items;
    }

    /** Legacy locale column form, e.g. {@code en_US} — matches {@link Locale#toString()}. */
    private static String db(Locale locale) {
        return locale.toString();
    }

    @Override
    public List<Category> findCategories(Locale locale) {
        return categories.findByLocaleOrderByIdAsc(db(locale)).stream().map(this::toCategory).toList();
    }

    @Override
    public Optional<Category> findCategory(String categoryId, Locale locale) {
        return categories.findByIdAndLocale(categoryId, db(locale)).map(this::toCategory);
    }

    @Override
    public List<Product> findProductsByCategory(String categoryId, Locale locale) {
        return products.findByCategoryAndLocale(categoryId, db(locale)).stream().map(this::toProduct).toList();
    }

    @Override
    public Optional<Product> findProduct(String productId, Locale locale) {
        return products.findByIdAndLocale(productId, db(locale)).map(this::toProduct);
    }

    @Override
    public List<Item> findItemsByProduct(String productId, Locale locale) {
        return items.findByProductAndLocale(productId, db(locale)).stream().map(this::toItem).toList();
    }

    @Override
    public Optional<Item> findItem(String itemId, Locale locale) {
        return items.findByIdAndLocale(itemId, db(locale)).map(this::toItem);
    }

    private Category toCategory(CategoryDetailEntity e) {
        return new Category(e.getId(), e.getName(), e.getImage());
    }

    private Product toProduct(ProductDetailEntity e) {
        return new Product(e.getId(), e.getProduct().getCatid(), e.getName(), e.getDescn(), e.getImage());
    }

    private Item toItem(ItemDetailEntity e) {
        return new Item(e.getId(), e.getItem().getProductid(), e.getListprice(), e.getUnitcost(),
                e.getImage(), e.getDescn(), e.attributes());
    }
}
