package com.example.petstore.catalog.application;

import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;

import java.util.List;

/**
 * Read model for the "view a product" use case: the product plus its purchasable items, resolved
 * for one locale. Mirrors the legacy product-detail page (a product and its size/variant items).
 */
public record ProductPage(Product product, List<Item> items) {
}
