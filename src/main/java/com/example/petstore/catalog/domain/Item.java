package com.example.petstore.catalog.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * A concrete, purchasable variant of a product (e.g. "Large Angelfish"), resolved for a locale.
 * Mirrors the legacy {@code item} + {@code item_details}, including per-locale pricing and the
 * up-to-five free-text attributes ({@code attr1..attr5}).
 */
public record Item(
        String id,
        String productId,
        BigDecimal listPrice,
        BigDecimal unitCost,
        String imageUrl,
        String description,
        List<String> attributes) {
}
