package com.example.petstore.cart.application;

/**
 * Raised when an item that is not in the catalog is added to the cart. The web adapter maps this
 * to HTTP 404 / a friendly error.
 */
public class UnknownItemException extends RuntimeException {

    public UnknownItemException(String itemId) {
        super("No such item: " + itemId);
    }
}
