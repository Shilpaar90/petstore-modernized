package com.example.petstore.catalog.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document for a product — one document per {@code productid}, the entity's real natural
 * key. {@code items} (the former standalone {@code catalog_items} collection) are embedded: a
 * product page always renders a product with all its SKU variants together, and no access pattern
 * ever queries an item independent of its product (ADR-0009). Locale-varying fields live in each
 * level's {@code i18n} map; {@code image} is locale-invariant in the legacy data.
 */
@Document(collection = "catalog_products")
public class ProductDocument {

    /** Embedded, locale-varying item/SKU fields (former {@code item_details} rows). */
    public static class ItemLocale {
        private BigDecimal listprice;
        private BigDecimal unitcost;
        private String descn;
        private List<String> attributes;

        protected ItemLocale() {
        }

        public ItemLocale(BigDecimal listprice, BigDecimal unitcost, String descn, List<String> attributes) {
            this.listprice = listprice;
            this.unitcost = unitcost;
            this.descn = descn;
            this.attributes = attributes;
        }

        public BigDecimal getListprice() {
            return listprice;
        }

        public BigDecimal getUnitcost() {
            return unitcost;
        }

        public String getDescn() {
            return descn;
        }

        public List<String> getAttributes() {
            return attributes == null ? List.of() : attributes;
        }
    }

    /** Embedded item/SKU (former standalone {@code catalog_items} document). */
    public static class ItemEntry {
        private String itemid;
        private String image;
        private Map<String, ItemLocale> i18n;

        protected ItemEntry() {
        }

        public ItemEntry(String itemid, String image, Map<String, ItemLocale> i18n) {
            this.itemid = itemid;
            this.image = image;
            this.i18n = i18n;
        }

        public String getItemid() {
            return itemid;
        }

        public String getImage() {
            return image;
        }

        public Map<String, ItemLocale> getI18n() {
            return i18n == null ? Map.of() : i18n;
        }
    }

    @Id
    private String productid;
    @Indexed
    private String catid;
    private String image;
    private Map<String, LocalizedText> i18n;
    private List<ItemEntry> items;

    protected ProductDocument() {
    }

    public ProductDocument(String productid, String catid, String image, Map<String, LocalizedText> i18n,
                           List<ItemEntry> items) {
        this.productid = productid;
        this.catid = catid;
        this.image = image;
        this.i18n = i18n;
        this.items = items;
    }

    public String getProductid() {
        return productid;
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

    public List<ItemEntry> getItems() {
        return items == null ? List.of() : items;
    }
}
