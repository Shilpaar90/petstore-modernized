package com.example.petstore.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end session-cart flow against the real catalog: add a real seeded item, then view the
 * cart and see it priced and totalled from live catalog data. The cart is anonymous (no login),
 * and its state is carried in the HTTP session.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CartFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void addItemThenSeeItPricedInTheCart() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // EST-1 (Angelfish) — en_US list price 16.50; add two.
        mvc.perform(post("/cart/add").param("itemId", "EST-1").param("quantity", "2")
                        .with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        mvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fresh Water fish from Japan")))
                .andExpect(content().string(containsString("33.00"))); // 2 * 16.50
    }

    @Test
    void addingAnUnknownItemIs404() throws Exception {
        mvc.perform(post("/cart/add").param("itemId", "NO-SUCH-ITEM")
                        .with(csrf()).session(new MockHttpSession()))
                .andExpect(status().isNotFound());
    }

    @Test
    void emptyCartRendersEmptyMessage() throws Exception {
        mvc.perform(get("/cart").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your cart is empty")));
    }
}
