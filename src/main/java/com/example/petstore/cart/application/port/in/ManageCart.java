package com.example.petstore.cart.application.port.in;

import com.example.petstore.cart.application.CartView;

import java.util.Locale;

/**
 * Inbound port for the session shopping-cart use case. Quantities live in the session; item
 * details and prices are resolved from the catalog on demand.
 */
public interface ManageCart {

    /**
     * Adds an item to the cart.
     *
     * @throws com.example.petstore.cart.application.UnknownItemException if the item id is not in the catalog
     */
    void add(String itemId, int quantity, Locale locale);

    void updateQuantity(String itemId, int quantity);

    void remove(String itemId);

    /** Empties the cart (e.g. after a successful checkout). */
    void clear();

    /** The current cart, priced and localized for display. */
    CartView view(Locale locale);
}
