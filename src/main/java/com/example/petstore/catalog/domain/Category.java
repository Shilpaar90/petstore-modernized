package com.example.petstore.catalog.domain;

/**
 * A top-level catalog grouping (Fish, Dogs, ...), resolved for a specific locale.
 * Framework-free domain value — mirrors the legacy {@code category} + {@code category_details}.
 */
public record Category(String id, String name, String imageUrl) {
}
