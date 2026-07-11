package com.example.petstore.order.application.port.in;

import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.ShippingDetails;

import java.util.Locale;

/**
 * Inbound port for the checkout use case: turn the current cart into a placed order.
 */
public interface PlaceOrder {

    /**
     * Places an order for {@code username} from their current cart.
     *
     * @throws com.example.petstore.order.application.EmptyCartException if the cart is empty
     */
    Order place(String username, ShippingDetails shipping, Locale locale);
}
