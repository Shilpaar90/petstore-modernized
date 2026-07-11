package com.example.petstore.order.domain;

import java.math.BigDecimal;

/**
 * A line on a placed order — a point-in-time snapshot of an item's description and price at
 * checkout (orders must not change if the catalog later does).
 */
public record OrderLine(String itemId, String description, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
