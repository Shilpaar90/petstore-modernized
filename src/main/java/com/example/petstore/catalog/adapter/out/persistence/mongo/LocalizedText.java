package com.example.petstore.catalog.adapter.out.persistence.mongo;

/**
 * The locale-varying fields shared by category and product documents. Embedded as the value type
 * of each document's {@code i18n} map — see ADR-0009.
 */
public record LocalizedText(String name, String descn) {
}
