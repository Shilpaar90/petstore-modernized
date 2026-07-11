package com.example.petstore.order;

import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.application.port.out.OrderSubmissionPort;
import com.example.petstore.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checkout against the real stack: an authenticated user adds a seeded item, places the
 * order, and lands on the confirmation. Asserts the order is persisted, the cart is emptied, and
 * the OPC seam ({@link OrderSubmissionPort}) is invoked — the whole point of the phase.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepository orders;

    // Replace the logging OPC adapter with a mock so we can assert the hand-off happened.
    @MockitoBean
    private OrderSubmissionPort orderSubmission;

    @Test
    void placeOrderEndToEnd() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/cart/add").param("itemId", "EST-1").param("quantity", "2")
                        .with(user("dave")).with(csrf()).session(session))
                .andExpect(redirectedUrl("/cart"));

        MvcResult placed = mvc.perform(post("/checkout")
                        .param("name", "Dave")
                        .param("addressLine", "1 Main St")
                        .param("city", "Springfield")
                        .param("email", "dave@example.com")
                        .with(user("dave")).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"))
                .andReturn();

        String location = placed.getResponse().getRedirectedUrl();
        String orderId = location.substring(location.lastIndexOf('/') + 1);

        // Persisted, owned by dave, priced from live catalog (2 * 16.50 = 33.00).
        Order saved = orders.findByOrderId(orderId).orElseThrow();
        assertThat(saved.username()).isEqualTo("dave");
        assertThat(saved.total()).isEqualByComparingTo("33.00");

        // OPC seam invoked.
        verify(orderSubmission).submit(any(Order.class));

        // Confirmation page is reachable by the owner and shows the order.
        mvc.perform(get(location).with(user("dave")).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(orderId)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("33.00")));

        // Cart is now empty.
        mvc.perform(get("/cart").with(user("dave")).session(session))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your cart is empty")));
    }

    @Test
    void anotherUserCannotSeeYourOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/cart/add").param("itemId", "EST-1")
                .with(user("owner")).with(csrf()).session(session));
        MvcResult placed = mvc.perform(post("/checkout")
                        .param("name", "Owner").param("addressLine", "1 St").param("city", "Town")
                        .param("email", "owner@example.com")
                        .with(user("owner")).with(csrf()).session(session))
                .andReturn();
        String location = placed.getResponse().getRedirectedUrl();

        mvc.perform(get(location).with(user("intruder")))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutRequiresAuthentication() throws Exception {
        mvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void checkoutWithEmptyCartRedirectsToCart() throws Exception {
        mvc.perform(get("/checkout").with(user("nobody")).session(new MockHttpSession()))
                .andExpect(redirectedUrl("/cart"));
    }
}
