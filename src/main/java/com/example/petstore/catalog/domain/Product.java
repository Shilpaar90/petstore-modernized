package com.example.petstore.catalog.domain;

/**
 * A product within a category (e.g. "Angelfish"), resolved for a specific locale.
 * Mirrors the legacy {@code product} + {@code product_details}.
 */
public record Product(String id, String categoryId, String name, String description, String imageUrl) {
}
