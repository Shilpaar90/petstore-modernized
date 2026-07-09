package com.example.petstore.catalog.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps the legacy {@code item} base table. Carries the {@code productid} that the localized
 * {@code item_details} rows lack.
 */
@Entity
@Table(name = "item")
public class ItemEntity {

    @Id
    @Column(name = "itemid")
    private String id;

    @Column(name = "productid")
    private String productid;

    protected ItemEntity() {
    }

    public String getId() {
        return id;
    }

    public String getProductid() {
        return productid;
    }
}
