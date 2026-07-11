package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB projection of a localized category — one document per (catid, locale). Unlike the
 * relational split of {@code category} + {@code category_details}, the document model folds the
 * locale detail into a single collection (see the denormalized seed).
 */
@Document(collection = "catalog_categories")
public class CategoryDocument {

    @Id
    private String id; // "{catid}|{locale}"
    private String catid;
    private String locale;
    private String name;
    private String image;
    private String descn;

    protected CategoryDocument() {
    }

    public CategoryDocument(String catid, String locale, String name, String image, String descn) {
        this.id = catid + "|" + locale;
        this.catid = catid;
        this.locale = locale;
        this.name = name;
        this.image = image;
        this.descn = descn;
    }

    public String getCatid() {
        return catid;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getDescn() {
        return descn;
    }
}
