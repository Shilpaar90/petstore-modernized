package com.example.petstore.catalog.adapter.in.web;

import com.example.petstore.catalog.application.CategoryPage;
import com.example.petstore.catalog.application.ProductPage;
import com.example.petstore.catalog.application.port.in.CatalogQuery;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import com.example.petstore.identity.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test for the read-only JSON API: verifies the serialized shape of the read models and
 * that unknown ids become HTTP 404. The use case is mocked.
 */
@WebMvcTest(CatalogRestController.class)
@Import(SecurityConfig.class)
class CatalogRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogQuery catalog;

    private static final Category FISH = new Category("FISH", "Fish", "fish_icon.gif");
    private static final Product ANGELFISH =
            new Product("FI-SW-01", "FISH", "Angelfish", "Salt Water fish from Australia", "fish1.jpg");
    private static final Item EST1 = new Item("EST-1", "FI-SW-01",
            new BigDecimal("16.50"), new BigDecimal("10.00"), "fish3.gif", "Large Angelfish", List.of("Large", "Cuddly"));

    @Test
    void categoriesJson() throws Exception {
        when(catalog.listCategories(any())).thenReturn(List.of(FISH));

        mvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("FISH"))
                .andExpect(jsonPath("$[0].name").value("Fish"));
    }

    @Test
    void categoryPageJson() throws Exception {
        when(catalog.viewCategory(eq("FISH"), any()))
                .thenReturn(Optional.of(new CategoryPage(FISH, List.of(ANGELFISH))));

        mvc.perform(get("/api/catalog/categories/FISH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.name").value("Fish"))
                .andExpect(jsonPath("$.products[0].id").value("FI-SW-01"));
    }

    @Test
    void productPageJson() throws Exception {
        when(catalog.viewProduct(eq("FI-SW-01"), any()))
                .thenReturn(Optional.of(new ProductPage(ANGELFISH, List.of(EST1))));

        mvc.perform(get("/api/catalog/products/FI-SW-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.name").value("Angelfish"))
                .andExpect(jsonPath("$.items[0].id").value("EST-1"))
                .andExpect(jsonPath("$.items[0].attributes[0]").value("Large"));
    }

    @Test
    void itemJson() throws Exception {
        when(catalog.viewItem(eq("EST-1"), any())).thenReturn(Optional.of(EST1));

        mvc.perform(get("/api/catalog/items/EST-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("EST-1"))
                .andExpect(jsonPath("$.productId").value("FI-SW-01"))
                .andExpect(jsonPath("$.attributes", org.hamcrest.Matchers.contains("Large", "Cuddly")));
    }

    @Test
    void unknownProductYields404() throws Exception {
        when(catalog.viewProduct(eq("NOPE"), any())).thenReturn(Optional.empty());

        mvc.perform(get("/api/catalog/products/NOPE")).andExpect(status().isNotFound());
    }
}
