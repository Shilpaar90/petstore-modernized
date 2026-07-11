package com.example.petstore.order.application.port.out;

import com.example.petstore.order.domain.Order;

/**
 * The anti-corruption seam that replaces the legacy asynchronous JMS hand-off to the Order
 * Processing Center (ADR-0006). Checkout persists the order, then submits it here. The default
 * adapter records the hand-off (no broker required); a real JMS/Kafka/HTTP adapter can replace it
 * with zero change to storefront logic.
 */
public interface OrderSubmissionPort {

    void submit(Order order);
}
