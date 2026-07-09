package com.example.petstore.catalog.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the legacy {@code product} base table. Carries the {@code catid} that the localized
 * {@code product_details} rows lack, so the adapter can populate {@code Product.categoryId}.
 */
@Entity
@Table(name = "product")
public class ProductEntity {

    @Id
    @Column(name = "productid")
    private String id;

    @Column(name = "catid")
    private String catid;

    protected ProductEntity() {
    }

    public String getId() {
        return id;
    }

    public String getCatid() {
        return catid;
    }
}
