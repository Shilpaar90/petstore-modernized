package com.example.petstore.catalog.adapter.in.web;

import com.example.petstore.catalog.application.port.in.CatalogQuery;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * Inbound web adapter: server-rendered (Thymeleaf) storefront browse pages. Depends only on the
 * inbound {@link CatalogQuery} port. The current {@link Locale} is resolved by Spring (see the
 * storefront's {@code SessionLocaleResolver} + {@code ?lang=} switch), so every page is localized.
 *
 * <p>Unknown ids map to HTTP 404 — the port returns {@link java.util.Optional#empty()} and this
 * adapter translates that to a {@link ResponseStatusException}.
 */
@Controller
public class CatalogViewController {

    private final CatalogQuery catalog;

    public CatalogViewController(CatalogQuery catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public String categories(Locale locale, Model model) {
        model.addAttribute("categories", catalog.listCategories(locale));
        return "catalog/categories";
    }

    @GetMapping("/categories/{categoryId}")
    public String category(@PathVariable String categoryId, Locale locale, Model model) {
        var page = catalog.viewCategory(categoryId, locale)
                .orElseThrow(() -> notFound("category", categoryId));
        model.addAttribute("category", page.category());
        model.addAttribute("products", page.products());
        return "catalog/category";
    }

    @GetMapping("/products/{productId}")
    public String product(@PathVariable String productId, Locale locale, Model model) {
        var page = catalog.viewProduct(productId, locale)
                .orElseThrow(() -> notFound("product", productId));
        model.addAttribute("product", page.product());
        model.addAttribute("items", page.items());
        return "catalog/product";
    }

    @GetMapping("/items/{itemId}")
    public String item(@PathVariable String itemId, Locale locale, Model model) {
        var item = catalog.viewItem(itemId, locale)
                .orElseThrow(() -> notFound("item", itemId));
        model.addAttribute("item", item);
        return "catalog/item";
    }

    private static ResponseStatusException notFound(String kind, String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No such " + kind + ": " + id);
    }
}
