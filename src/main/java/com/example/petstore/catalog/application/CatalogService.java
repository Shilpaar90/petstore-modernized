package com.example.petstore.catalog.application;

import com.example.petstore.catalog.application.port.in.CatalogQuery;
import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The catalog browsing use case: implements the inbound {@link CatalogQuery} port by composing
 * reads from the outbound {@link CatalogRepository} port. This is where the hierarchy is
 * assembled (a category with its products, a product with its items) so adapters stay thin.
 */
@Service
public class CatalogService implements CatalogQuery {

    private final CatalogRepository repository;

    public CatalogService(CatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> listCategories(Locale locale) {
        return repository.findCategories(locale);
    }

    @Override
    public Optional<CategoryPage> viewCategory(String categoryId, Locale locale) {
        return repository.findCategory(categoryId, locale)
                .map(category -> new CategoryPage(category, repository.findProductsByCategory(categoryId, locale)));
    }

    @Override
    public Optional<ProductPage> viewProduct(String productId, Locale locale) {
        return repository.findProduct(productId, locale)
                .map(product -> new ProductPage(product, repository.findItemsByProduct(productId, locale)));
    }

    @Override
    public Optional<Item> viewItem(String itemId, Locale locale) {
        return repository.findItem(itemId, locale);
    }
}
