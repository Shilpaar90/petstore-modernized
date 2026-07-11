package com.example.petstore.catalog.adapter.in.web;

import com.example.petstore.catalog.application.CategoryPage;
import com.example.petstore.catalog.application.ProductPage;
import com.example.petstore.catalog.application.port.in.CatalogQuery;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web slice test for the Thymeleaf browse controller: verifies routing, view + model wiring, and
 * that a missing id becomes HTTP 404. The use case is mocked, so this exercises only the adapter.
 */
@WebMvcTest(CatalogViewController.class)
@AutoConfigureMockMvc(addFilters = false) // controller slice — security rules covered by the full-context flow tests
class CatalogViewControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogQuery catalog;

    private static final Category FISH = new Category("FISH", "Fish", "fish_icon.gif");
    private static final Product ANGELFISH =
            new Product("FI-SW-01", "FISH", "Angelfish", "Salt Water fish from Australia", "fish1.jpg");
    private static final Item EST1 = new Item("EST-1", "FI-SW-01",
            new BigDecimal("16.50"), new BigDecimal("10.00"), "fish3.gif", "Large Angelfish", List.of("Large"));

    @Test
    void categoriesPageListsCategories() throws Exception {
        when(catalog.listCategories(any())).thenReturn(List.of(FISH));

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/categories"))
                .andExpect(model().attribute("categories", List.of(FISH)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fish")));
    }

    @Test
    void categoryPageShowsProducts() throws Exception {
        when(catalog.viewCategory(eq("FISH"), any()))
                .thenReturn(Optional.of(new CategoryPage(FISH, List.of(ANGELFISH))));

        mvc.perform(get("/categories/FISH"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/category"))
                .andExpect(model().attribute("category", FISH))
                .andExpect(model().attribute("products", List.of(ANGELFISH)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Angelfish")));
    }

    @Test
    void productPageShowsItems() throws Exception {
        when(catalog.viewProduct(eq("FI-SW-01"), any()))
                .thenReturn(Optional.of(new ProductPage(ANGELFISH, List.of(EST1))));

        mvc.perform(get("/products/FI-SW-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/product"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("EST-1")));
    }

    @Test
    void itemPageShowsDetail() throws Exception {
        when(catalog.viewItem(eq("EST-1"), any())).thenReturn(Optional.of(EST1));

        mvc.perform(get("/items/EST-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/item"))
                .andExpect(model().attribute("item", EST1));
    }

    @Test
    void unknownCategoryYields404() throws Exception {
        when(catalog.viewCategory(eq("NOPE"), any())).thenReturn(Optional.empty());

        mvc.perform(get("/categories/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void unknownItemYields404() throws Exception {
        when(catalog.viewItem(eq("EST-999"), any())).thenReturn(Optional.empty());

        mvc.perform(get("/items/EST-999")).andExpect(status().isNotFound());
    }
}
