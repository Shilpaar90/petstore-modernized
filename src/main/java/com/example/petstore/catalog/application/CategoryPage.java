package com.example.petstore.catalog.application;

import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Product;

import java.util.List;

/**
 * Read model for the "browse a category" use case: the category itself plus the products it
 * contains, all resolved for one locale. Composed by {@link com.example.petstore.catalog.application.CatalogService}
 * from the outbound port so the web layer gets exactly what a category page needs in one call.
 */
public record CategoryPage(Category category, List<Product> products) {
}
