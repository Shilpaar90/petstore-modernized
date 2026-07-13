package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * MongoDB document for a catalog category — one document per {@code catid}, the entity's real
 * natural key. Locale-varying fields ({@code name}, {@code descn}) live in the {@code i18n} map;
 * {@code image} is locale-invariant in the legacy data and stays top-level (see ADR-0009, which
 * replaced the earlier one-document-per-{@code (catid, locale)} partitioning).
 */
@Document(collection = "catalog_categories")
public class CategoryDocument {

    @Id
    private String catid;
    private String image;
    private Map<String, LocalizedText> i18n;

    protected CategoryDocument() {
    }

    public CategoryDocument(String catid, String image, Map<String, LocalizedText> i18n) {
        this.catid = catid;
        this.image = image;
        this.i18n = i18n;
    }

    public String getCatid() {
        return catid;
    }

    public String getImage() {
        return image;
    }

    public Map<String, LocalizedText> getI18n() {
        return i18n == null ? Map.of() : i18n;
    }
}
