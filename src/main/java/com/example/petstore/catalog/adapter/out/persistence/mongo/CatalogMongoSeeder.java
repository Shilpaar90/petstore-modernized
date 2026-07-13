package com.example.petstore.catalog.adapter.out.persistence.mongo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the MongoDB catalog on {@code mongo}-profile startup from
 * {@code db/mongo/catalog-seed.json} — the document counterpart of the relational Flyway V2 seed,
 * generated from the SAME legacy XML by {@code tools/seed-import/extract_catalog_seed.py}
 * ({@code --format mongo}). The JSON is already shaped like the target documents (one entry per
 * product with embedded items and a keyed {@code i18n} map, see ADR-0009), so seeding is a direct
 * read, not a grouping/aggregation step. Idempotent: only seeds when the catalog is empty.
 */
@Component
@Profile("mongo")
public class CatalogMongoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogMongoSeeder.class);
    private static final String SEED = "db/mongo/catalog-seed.json";

    private final CategoryDocumentRepository categories;
    private final ProductDocumentRepository products;
    private final ObjectMapper objectMapper;

    public CatalogMongoSeeder(CategoryDocumentRepository categories,
                              ProductDocumentRepository products,
                              ObjectMapper objectMapper) {
        this.categories = categories;
        this.products = products;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (categories.count() > 0) {
            log.info("Mongo catalog already seeded ({} category docs) — skipping", categories.count());
            return;
        }

        JsonNode root;
        try (InputStream in = new ClassPathResource(SEED).getInputStream()) {
            root = objectMapper.readTree(in);
        }

        List<CategoryDocument> cats = new ArrayList<>();
        for (JsonNode c : root.path("categories")) {
            cats.add(new CategoryDocument(c.path("catid").asText(), text(c, "image"), textI18n(c.path("i18n"))));
        }

        List<ProductDocument> prods = new ArrayList<>();
        int itemCount = 0;
        for (JsonNode p : root.path("products")) {
            List<ProductDocument.ItemEntry> items = new ArrayList<>();
            for (JsonNode i : p.path("items")) {
                items.add(new ProductDocument.ItemEntry(i.path("itemid").asText(), text(i, "image"), itemI18n(i.path("i18n"))));
                itemCount++;
            }
            prods.add(new ProductDocument(p.path("productid").asText(), p.path("catid").asText(),
                    text(p, "image"), textI18n(p.path("i18n")), items));
        }

        categories.saveAll(cats);
        products.saveAll(prods);
        log.info("Seeded Mongo catalog: {} categories, {} products, {} items", cats.size(), prods.size(), itemCount);
    }

    private static Map<String, LocalizedText> textI18n(JsonNode i18nNode) {
        Map<String, LocalizedText> map = new LinkedHashMap<>();
        i18nNode.properties().forEach(e -> {
            JsonNode v = e.getValue();
            map.put(e.getKey(), new LocalizedText(text(v, "name"), text(v, "descn")));
        });
        return map;
    }

    private static Map<String, ProductDocument.ItemLocale> itemI18n(JsonNode i18nNode) {
        Map<String, ProductDocument.ItemLocale> map = new LinkedHashMap<>();
        i18nNode.properties().forEach(e -> {
            JsonNode v = e.getValue();
            List<String> attrs = new ArrayList<>();
            v.path("attributes").forEach(a -> attrs.add(a.asText()));
            map.put(e.getKey(), new ProductDocument.ItemLocale(
                    new BigDecimal(v.path("listprice").asText()), new BigDecimal(v.path("unitcost").asText()),
                    text(v, "descn"), attrs));
        });
        return map;
    }

    /** Null-safe text: JSON null -> Java null (preserves the relational NULLs, e.g. category descn). */
    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
