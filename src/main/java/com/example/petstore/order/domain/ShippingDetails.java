package com.example.petstore.order.domain;

/**
 * Where the order ships. A trimmed stand-in for the legacy {@code ContactInfo}/{@code Address}
 * CMP beans — enough to make checkout realistic and the confirmation meaningful.
 */
public record ShippingDetails(String name, String addressLine, String city, String email) {
}
