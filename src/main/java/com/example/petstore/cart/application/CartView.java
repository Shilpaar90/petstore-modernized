package com.example.petstore.cart.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * A rendered snapshot of the cart: priced lines plus the roll-up total and unit count, all
 * resolved for one locale.
 */
public record CartView(List<CartLine> lines, BigDecimal total, int unitCount) {

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
