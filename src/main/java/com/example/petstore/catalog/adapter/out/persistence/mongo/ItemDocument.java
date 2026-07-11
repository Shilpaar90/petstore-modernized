package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

/**
 * MongoDB projection of a localized item — one document per (itemid, locale), with the owning
 * {@code productid} denormalized on and the legacy attr1..attr5 folded into an array.
 */
@Document(collection = "catalog_items")
public class ItemDocument {

    @Id
    private String id; // "{itemid}|{locale}"
    private String itemid;
    private String productid;
    private String locale;
    private BigDecimal listprice;
    private BigDecimal unitcost;
    private String image;
    private String descn;
    private List<String> attributes;

    protected ItemDocument() {
    }

    public ItemDocument(String itemid, String productid, String locale, BigDecimal listprice, BigDecimal unitcost,
                        String image, String descn, List<String> attributes) {
        this.id = itemid + "|" + locale;
        this.itemid = itemid;
        this.productid = productid;
        this.locale = locale;
        this.listprice = listprice;
        this.unitcost = unitcost;
        this.image = image;
        this.descn = descn;
        this.attributes = attributes;
    }

    public String getItemid() {
        return itemid;
    }

    public String getProductid() {
        return productid;
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

    public List<String> getAttributes() {
        return attributes == null ? List.of() : attributes;
    }
}
