package com.example.petstore.mongo;

import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exit-criterion test for Phase 5: the ENTIRE storefront runs on MongoDB. Registration + login go
 * through the Mongo user adapter; browsing reads the Mongo catalog; checkout writes a Mongo order.
 * Same controllers, same use cases as the JPA profile — only the persistence edge changed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
class MongoAppFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepository orders;

    @Test
    void registerLoginBrowseAndCheckoutAllOnMongo() throws Exception {
        // Registration persists to Mongo; login authenticates via the Mongo-backed UserDetailsService.
        mvc.perform(post("/register").param("username", "mavis").param("password", "secret123").with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(formLogin("/login").user("mavis").password("secret123"))
                .andExpect(authenticated().withUsername("mavis"));

        // Catalog served from Mongo.
        mvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fish")));

        // Cart + checkout on Mongo.
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cart/add").param("itemId", "EST-1").param("quantity", "2")
                .with(user("mavis")).with(csrf()).session(session));

        MvcResult placed = mvc.perform(post("/checkout")
                        .param("name", "Mavis").param("addressLine", "1 Main St")
                        .param("city", "Springfield").param("email", "mavis@example.com")
                        .with(user("mavis")).with(csrf()).session(session))
                .andExpect(redirectedUrlPattern("/orders/*"))
                .andReturn();

        String location = placed.getResponse().getRedirectedUrl();
        String orderId = location.substring(location.lastIndexOf('/') + 1);

        Order saved = orders.findByOrderId(orderId).orElseThrow();
        assertThat(saved.username()).isEqualTo("mavis");
        assertThat(saved.total()).isEqualByComparingTo("33.00"); // 2 * 16.50 from the Mongo catalog

        mvc.perform(get(location).with(user("mavis")).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("33.00")));

        mvc.perform(get("/cart").with(user("mavis")).session(session))
                .andExpect(content().string(containsString("Your cart is empty")));
    }
}
