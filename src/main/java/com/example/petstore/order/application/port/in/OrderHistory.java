package com.example.petstore.order.application.port.in;

import com.example.petstore.order.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port for reading a user's own orders (confirmation page + history).
 */
public interface OrderHistory {

    List<Order> forUser(String username);

    /** An order, but only if it belongs to {@code username} (authorization guard). */
    Optional<Order> find(String orderId, String username);
}
