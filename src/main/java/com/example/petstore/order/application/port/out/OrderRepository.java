package com.example.petstore.order.application.port.out;

import com.example.petstore.order.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for order persistence — the durable order records the legacy storefront lacked
 * (ADR-0006). JPA implements it today; MongoDB can later (Phase 5).
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findByOrderId(String orderId);

    /** A user's orders, most recent first. */
    List<Order> findByUsername(String username);
}
