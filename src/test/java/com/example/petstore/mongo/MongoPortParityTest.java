package com.example.petstore.mongo;

import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import com.example.petstore.identity.application.StoredUser;
import com.example.petstore.identity.application.port.out.UserRepository;
import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.OrderLine;
import com.example.petstore.order.domain.OrderStatus;
import com.example.petstore.order.domain.ShippingDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The hexagonal payoff: on the {@code mongo} profile, the SAME outbound ports are satisfied by the
 * MongoDB adapters, with the SAME observable behavior as the JPA adapters. The catalog assertions
 * intentionally mirror {@code CatalogRepositoryJpaAdapterCharacterizationTest} — a contract both
 * stores must honor. Runs the whole app on embedded MongoDB (the catalog seeded from the shared
 * document seed).
 */
@SpringBootTest
@ActiveProfiles("mongo")
class MongoPortParityTest {

    private static final Locale EN = Locale.of("en", "US");
    private static final Locale JA = Locale.of("ja", "JP");

    @Autowired
    private CatalogRepository catalog;
    @Autowired
    private UserRepository users;
    @Autowired
    private OrderRepository orders;

    @Test
    void catalogParityMatchesTheLegacyFacts() {
        assertThat(catalog.findCategories(EN)).extracting(Category::id)
                .containsExactly("BIRDS", "CATS", "DOGS", "FISH", "REPTILES");

        assertThat(catalog.findCategory("FISH", JA)).map(Category::name).contains("魚"); // 魚

        assertThat(catalog.findProductsByCategory("FISH", EN)).extracting(Product::id)
                .containsExactly("FI-FW-01", "FI-FW-02", "FI-SW-01", "FI-SW-02");

        Product angelfish = catalog.findProduct("FI-SW-01", EN).orElseThrow();
        assertThat(angelfish.name()).isEqualTo("Angelfish");
        assertThat(angelfish.categoryId()).isEqualTo("FISH");

        assertThat(catalog.findItemsByProduct("FI-SW-01", EN)).extracting(Item::id)
                .containsExactly("EST-1", "EST-2");

        Item est1 = catalog.findItem("EST-1", EN).orElseThrow();
        assertThat(est1.listPrice()).isEqualByComparingTo("16.50");
        assertThat(est1.attributes()).containsExactly("Large", "Cuddly");

        Item est1Ja = catalog.findItem("EST-1", JA).orElseThrow();
        assertThat(est1Ja.listPrice()).isEqualByComparingTo(new BigDecimal("1951"));
        assertThat(est1Ja.description()).isEqualTo("日本産の淡水魚"); // 日本産の淡水魚

        assertThat(catalog.findItem("NOPE", EN)).isEmpty();
    }

    @Test
    void userPortRoundTrips() {
        users.create("mongo-user", "$2a$fakehash", true);

        assertThat(users.existsByUsername("mongo-user")).isTrue();
        assertThat(users.findByUsername("mongo-user")).get()
                .extracting(StoredUser::passwordHash).isEqualTo("$2a$fakehash");
        assertThat(users.findByUsername("ghost")).isEmpty();
    }

    @Test
    void orderPortRoundTrips() {
        Order order = new Order("mord-1", "alice",
                List.of(new OrderLine("EST-1", "Large Angelfish", new BigDecimal("16.50"), 2, new BigDecimal("33.00"))),
                new BigDecimal("33.00"), OrderStatus.SUBMITTED, Instant.now(),
                new ShippingDetails("Alice", "1 Main St", "Springfield", "alice@example.com"));

        orders.save(order);

        Order reloaded = orders.findByOrderId("mord-1").orElseThrow();
        assertThat(reloaded.username()).isEqualTo("alice");
        assertThat(reloaded.total()).isEqualByComparingTo("33.00");
        assertThat(reloaded.lines()).singleElement()
                .satisfies(l -> assertThat(l.itemId()).isEqualTo("EST-1"));
        assertThat(reloaded.shipping().city()).isEqualTo("Springfield");
        assertThat(orders.findByUsername("alice")).extracting(Order::orderId).contains("mord-1");
    }
}
