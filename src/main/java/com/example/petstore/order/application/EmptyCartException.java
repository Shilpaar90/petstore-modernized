package com.example.petstore.order.application;

/**
 * Raised when checkout is attempted with an empty cart. The web adapter turns this into a
 * redirect back to the cart rather than an error.
 */
public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot place an order with an empty cart");
    }
}
