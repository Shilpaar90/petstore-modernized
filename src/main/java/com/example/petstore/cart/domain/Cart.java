package com.example.petstore.cart.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A shopping cart: item id → quantity, insertion-ordered. Pure domain aggregate — no Spring, no
 * catalog/pricing knowledge (prices are resolved for display by the application layer, so the
 * cart always reflects current catalog prices, matching the legacy stateful session cart).
 */
public class Cart {

    private final Map<String, Integer> quantities = new LinkedHashMap<>();

    /** Adds {@code quantity} of an item, accumulating with any existing line. */
    public void add(String itemId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        quantities.merge(itemId, quantity, Integer::sum);
    }

    /** Sets an item's absolute quantity; a quantity of zero or less removes the line. */
    public void setQuantity(String itemId, int quantity) {
        if (quantity <= 0) {
            quantities.remove(itemId);
        } else {
            quantities.put(itemId, quantity);
        }
    }

    public void remove(String itemId) {
        quantities.remove(itemId);
    }

    public void clear() {
        quantities.clear();
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    /** Total number of units across all lines. */
    public int unitCount() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** An unmodifiable, insertion-ordered view of item id → quantity. */
    public Map<String, Integer> quantities() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(quantities));
    }
}
