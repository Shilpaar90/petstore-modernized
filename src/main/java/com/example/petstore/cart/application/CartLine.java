package com.example.petstore.cart.application;

import java.math.BigDecimal;

/**
 * A single priced cart line for display: the item, its resolved description and current unit
 * price (from the catalog), the quantity, and the extended line total.
 */
public record CartLine(String itemId, String description, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
