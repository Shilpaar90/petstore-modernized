package com.example.petstore.catalog.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Maps the legacy {@code category_details} table (base {@code category} carries no extra
 * columns, so it needs no entity of its own for the read path).
 */
@Entity
@Table(name = "category_details")
@IdClass(LocalizedId.class)
public class CategoryDetailEntity {

    @Id
    @Column(name = "catid")
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

    protected CategoryDetailEntity() {
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
}
