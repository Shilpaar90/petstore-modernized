package com.example.petstore.catalog.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Maps the legacy {@code product_details} table. The read-only {@code @ManyToOne} to
 * {@link ProductEntity} (over the same {@code productid} column) lets queries filter by the
 * owning product's {@code catid} without that column existing on this table.
 */
@Entity
@Table(name = "product_details")
@IdClass(LocalizedId.class)
public class ProductDetailEntity {

    @Id
    @Column(name = "productid")
    private String id;

    @Id
    @Column(name = "locale")
    private String locale;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "descn")
    private String descn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productid", insertable = false, updatable = false)
    private ProductEntity product;

    protected ProductDetailEntity() {
    }

    public String getId() {
        return id;
    }

    public String getLocale() {
        return locale;
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

    public ProductEntity getProduct() {
        return product;
    }
}
