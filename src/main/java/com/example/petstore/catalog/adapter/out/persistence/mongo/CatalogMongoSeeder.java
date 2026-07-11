package com.example.petstore.catalog.adapter.out.persistence.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

/**
 * Seeds the MongoDB catalog on {@code mongo}-profile startup from
 * {@code db/mongo/catalog-seed.json} — the document counterpart of the relational Flyway V2 seed,
 * generated from the SAME legacy XML by {@code tools/seed-import/extract_catalog_seed.py}
 * ({@code --format mongo}). Idempotent: only seeds when the catalog is empty, so repeated boots
 * (and test contexts) don't duplicate. This is the document-store arm of the data-migration story
 * in ADR-0006 / the target architecture.
 */
@Component
@Profile("mongo")
public class CatalogMongoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogMongoSeeder.class);
    private static final String SEED = "db/mongo/catalog-seed.json";

    private final CategoryDocumentRepository categories;
    private final ProductDocumentRepository products;
    private final ItemDocumentRepository items;
    private final ObjectMapper objectMapper;

    public CatalogMongoSeeder(CategoryDocumentRepository categories,
                              ProductDocumentRepository products,
                              ItemDocumentRepository items,
                              ObjectMapper objectMapper) {
        this.categories = categories;
        this.products = products;
        this.items = items;
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
            cats.add(new CategoryDocument(c.path("catid").asText(), c.path("locale").asText(),
                    text(c, "name"), text(c, "image"), text(c, "descn")));
        }
        List<ProductDocument> prods = new ArrayList<>();
        for (JsonNode p : root.path("products")) {
            prods.add(new ProductDocument(p.path("productid").asText(), p.path("catid").asText(),
                    p.path("locale").asText(), text(p, "name"), text(p, "image"), text(p, "descn")));
        }
        List<ItemDocument> its = new ArrayList<>();
        for (JsonNode i : root.path("items")) {
            List<String> attrs = new ArrayList<>();
            i.path("attributes").forEach(a -> attrs.add(a.asText()));
            its.add(new ItemDocument(i.path("itemid").asText(), i.path("productid").asText(),
                    i.path("locale").asText(), new BigDecimal(i.path("listprice").asText()),
                    new BigDecimal(i.path("unitcost").asText()), text(i, "image"), text(i, "descn"), attrs));
        }

        categories.saveAll(cats);
        products.saveAll(prods);
        items.saveAll(its);
        log.info("Seeded Mongo catalog: {} categories, {} products, {} items",
                cats.size(), prods.size(), its.size());
    }

    /** Null-safe text: JSON null -> Java null (preserves the relational NULLs, e.g. category descn). */
    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
