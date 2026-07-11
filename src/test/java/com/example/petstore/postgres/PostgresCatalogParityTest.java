package com.example.petstore.postgres;

import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prod-like verification (ADR-0007): the relational migration runs on REAL PostgreSQL, not just
 * H2. Testcontainers boots a Postgres, {@code @ServiceConnection} points the app's datasource at
 * it, Flyway applies the identical V1..V4 migrations, and the JPA adapter must reproduce the same
 * legacy catalog facts as everywhere else.
 *
 * <p>Skipped automatically when Docker is unavailable (e.g. a plain laptop build), so local
 * {@code mvn test} stays green; CI runs it with Docker present.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresCatalogParityTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    private static final Locale EN = Locale.of("en", "US");
    private static final Locale JA = Locale.of("ja", "JP");

    @Autowired
    private CatalogRepository catalog;

    @Test
    void catalogMigratesAndReadsCorrectlyOnPostgres() {
        assertThat(catalog.findCategories(EN)).extracting(Category::id)
                .containsExactly("BIRDS", "CATS", "DOGS", "FISH", "REPTILES");

        assertThat(catalog.findCategory("FISH", JA)).map(Category::name).contains("魚"); // 魚

        Item est1 = catalog.findItem("EST-1", EN).orElseThrow();
        assertThat(est1.listPrice()).isEqualByComparingTo("16.50");
        assertThat(est1.attributes()).containsExactly("Large", "Cuddly");

        assertThat(catalog.findItem("EST-1", JA).orElseThrow().listPrice())
                .isEqualByComparingTo(new BigDecimal("1951"));
    }
}
