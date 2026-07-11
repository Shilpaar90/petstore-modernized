package com.example.petstore.order.adapter.out.persistence.jpa;

import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.OrderLine;
import com.example.petstore.order.domain.OrderStatus;
import com.example.petstore.order.domain.ShippingDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistence round-trip for the order aggregate against the real Flyway schema: save an order
 * with lines, then reload it through the port and assert every field (and the owned lines)
 * survived — the mapping's quality gate.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderRepositoryJpaAdapter.class)
class OrderPersistenceTest {

    @Autowired
    private OrderRepository orders;

    @Test
    void savesAndReloadsAnOrderWithItsLines() {
        Instant when = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Order order = new Order(
                "ord-100", "alice",
                List.of(
                        new OrderLine("EST-1", "Large Angelfish", new BigDecimal("16.50"), 2, new BigDecimal("33.00")),
                        new OrderLine("EST-2", "Small Angelfish", new BigDecimal("10.00"), 1, new BigDecimal("10.00"))),
                new BigDecimal("43.00"), OrderStatus.SUBMITTED, when,
                new ShippingDetails("Alice", "1 Main St", "Springfield", "alice@example.com"));

        orders.save(order);

        Order reloaded = orders.findByOrderId("ord-100").orElseThrow();
        assertThat(reloaded.username()).isEqualTo("alice");
        assertThat(reloaded.status()).isEqualTo(OrderStatus.SUBMITTED);
        assertThat(reloaded.total()).isEqualByComparingTo("43.00");
        assertThat(reloaded.shipping().city()).isEqualTo("Springfield");
        assertThat(reloaded.lines()).hasSize(2);
        assertThat(reloaded.lines()).extracting(OrderLine::itemId).containsExactly("EST-1", "EST-2");
        assertThat(reloaded.lines().get(0).lineTotal()).isEqualByComparingTo("33.00");
    }

    @Test
    void findsAUsersOrders() {
        orders.save(new Order("ord-a", "bob", List.of(), new BigDecimal("1.00"),
                OrderStatus.SUBMITTED, Instant.now(), new ShippingDetails("Bob", "x", "y", "b@e.com")));

        assertThat(orders.findByUsername("bob")).extracting(Order::orderId).contains("ord-a");
        assertThat(orders.findByUsername("nobody")).isEmpty();
    }
}
