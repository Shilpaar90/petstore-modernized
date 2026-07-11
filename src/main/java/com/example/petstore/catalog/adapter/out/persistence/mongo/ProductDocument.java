package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB projection of a localized product — one document per (productid, locale), with the
 * owning {@code catid} denormalized on (no join needed to browse a category).
 */
@Document(collection = "catalog_products")
public class ProductDocument {

    @Id
    private String id; // "{productid}|{locale}"
    private String productid;
    private String catid;
    private String locale;
    private String name;
    private String image;
    private String descn;

    protected ProductDocument() {
    }

    public ProductDocument(String productid, String catid, String locale, String name, String image, String descn) {
        this.id = productid + "|" + locale;
        this.productid = productid;
        this.catid = catid;
        this.locale = locale;
        this.name = name;
        this.image = image;
        this.descn = descn;
    }

    public String getProductid() {
        return productid;
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
