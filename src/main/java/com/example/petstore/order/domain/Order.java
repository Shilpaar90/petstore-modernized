package com.example.petstore.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A placed order: who placed it, the priced line snapshot, the roll-up total, status, when, and
 * where it ships. Unlike the legacy storefront (which persisted nothing and streamed orders to
 * the OPC), we keep a durable record — an intentional enhancement, see ADR-0006 / risk R8.
 */
public record Order(
        String orderId,
        String username,
        List<OrderLine> lines,
        BigDecimal total,
        OrderStatus status,
        Instant placedAt,
        ShippingDetails shipping) {
}
