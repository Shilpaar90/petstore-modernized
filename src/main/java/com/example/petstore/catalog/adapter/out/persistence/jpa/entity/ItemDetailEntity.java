package com.example.petstore.catalog.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps the legacy {@code item_details} table, including per-locale {@code listprice}/{@code unitcost}
 * and the five positional attribute columns ({@code attr1..attr5}).
 */
@Entity
@Table(name = "item_details")
@IdClass(LocalizedId.class)
public class ItemDetailEntity {

    @Id
    @Column(name = "itemid")
    private String id;

    @Id
    @Column(name = "locale")
    private String locale;

    @Column(name = "listprice")
    private BigDecimal listprice;

    @Column(name = "unitcost")
    private BigDecimal unitcost;

    @Column(name = "image")
    private String image;

    @Column(name = "descn")
    private String descn;

    @Column(name = "attr1")
    private String attr1;

    @Column(name = "attr2")
    private String attr2;

    @Column(name = "attr3")
    private String attr3;

    @Column(name = "attr4")
    private String attr4;

    @Column(name = "attr5")
    private String attr5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemid", insertable = false, updatable = false)
    private ItemEntity item;

    protected ItemDetailEntity() {
    }

    public String getId() {
        return id;
    }

    public String getLocale() {
        return locale;
    }

    public BigDecimal getListprice() {
        return listprice;
    }

    public BigDecimal getUnitcost() {
        return unitcost;
    }

    public String getImage() {
        return image;
    }

    public String getDescn() {
        return descn;
    }

    public ItemEntity getItem() {
        return item;
    }

    /** The non-null attributes, in positional order — the modern face of {@code attr1..attr5}. */
    public List<String> attributes() {
        List<String> attrs = new ArrayList<>(5);
        for (String a : new String[] {attr1, attr2, attr3, attr4, attr5}) {
            if (a != null && !a.isBlank()) {
                attrs.add(a);
            }
        }
        return attrs;
    }
}
