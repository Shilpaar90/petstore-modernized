package com.example.petstore.catalog.adapter.in.web;

import com.example.petstore.catalog.application.CategoryPage;
import com.example.petstore.catalog.application.ProductPage;
import com.example.petstore.catalog.application.port.in.CatalogQuery;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

/**
 * Inbound web adapter: read-only JSON API over the same {@link CatalogQuery} use case that backs
 * the HTML views. Locale is negotiated the same way (resolver + {@code ?lang=}), so the API is
 * localized identically to the UI. Unknown ids yield HTTP 404.
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogRestController {

    private final CatalogQuery catalog;

    public CatalogRestController(CatalogQuery catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<Category> categories(Locale locale) {
        return catalog.listCategories(locale);
    }

    @GetMapping("/categories/{categoryId}")
    public CategoryPage category(@PathVariable String categoryId, Locale locale) {
        return catalog.viewCategory(categoryId, locale)
                .orElseThrow(() -> notFound("category", categoryId));
    }

    @GetMapping("/products/{productId}")
    public ProductPage product(@PathVariable String productId, Locale locale) {
        return catalog.viewProduct(productId, locale)
                .orElseThrow(() -> notFound("product", productId));
    }

    @GetMapping("/items/{itemId}")
    public Item item(@PathVariable String itemId, Locale locale) {
        return catalog.viewItem(itemId, locale)
                .orElseThrow(() -> notFound("item", itemId));
    }

    private static ResponseStatusException notFound(String kind, String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No such " + kind + ": " + id);
    }
}
