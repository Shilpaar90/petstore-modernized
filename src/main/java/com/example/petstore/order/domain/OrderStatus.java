package com.example.petstore.order.domain;

/**
 * Lifecycle state of an order. The storefront only ever creates {@code SUBMITTED} orders (placed
 * and handed off to the OPC seam); downstream states are owned by the out-of-scope OPC.
 */
public enum OrderStatus {
    SUBMITTED
}
