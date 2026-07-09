package com.example.petstore.catalog.adapter.out.persistence.jpa;

import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization ("parity") tests for the relational catalog adapter — Phase 1's data-layer
 * quality gate. These pin the <em>legacy</em> catalog's observable behavior: the assertions are
 * facts about the original Java Pet Store 1.3.1 data (from {@code Populate-UTF8.xml}), not about
 * this implementation. If a future refactor — including the MongoDB re-platform (Phase 5), which
 * must satisfy the same {@link CatalogRepository} contract — changes any of these answers, that is
 * a regression, and this suite fails.
 *
 * <p>Runs the <b>real</b> configured H2 datasource with the actual Flyway migrations
 * ({@code @AutoConfigureTestDatabase(replace = NONE)}) so we exercise the true schema + seed, and
 * imports only the adapter under test on top of the JPA slice.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CatalogRepositoryJpaAdapter.class)
class CatalogRepositoryJpaAdapterCharacterizationTest {

    private static final Locale EN = Locale.of("en", "US");
    private static final Locale JA = Locale.of("ja", "JP");
    private static final Locale ZH = Locale.of("zh", "CN");

    @Autowired
    private CatalogRepository catalog;

    @Nested
    @DisplayName("categories")
    class Categories {

        @Test
        @DisplayName("the legacy storefront has exactly five categories, ordered by id")
        void fiveCategoriesOrderedById() {
            List<Category> categories = catalog.findCategories(EN);

            assertThat(categories).extracting(Category::id)
                    .containsExactly("BIRDS", "CATS", "DOGS", "FISH", "REPTILES");
            assertThat(categories).extracting(Category::name)
                    .containsExactly("Birds", "Cats", "Dogs", "Fish", "Reptiles");
        }

        @Test
        @DisplayName("category names resolve per locale (i18n side-table)")
        void categoryNamesAreLocalized() {
            assertThat(catalog.findCategory("FISH", EN)).map(Category::name).contains("Fish");
            assertThat(catalog.findCategory("FISH", JA)).map(Category::name).contains("魚"); // 魚
            assertThat(catalog.findCategory("FISH", ZH)).map(Category::name).contains("鱼"); // 鱼
        }

        @Test
        @DisplayName("an unknown category, or a category with no row for the locale, is absent")
        void missingCategoryIsEmpty() {
            assertThat(catalog.findCategory("NOPE", EN)).isEmpty();
            assertThat(catalog.findCategory("FISH", Locale.of("fr", "FR"))).isEmpty();
        }
    }

    @Nested
    @DisplayName("products")
    class Products {

        @Test
        @DisplayName("the FISH category holds its four legacy products")
        void fishHasFourProducts() {
            assertThat(catalog.findProductsByCategory("FISH", EN)).extracting(Product::id)
                    .containsExactly("FI-FW-01", "FI-FW-02", "FI-SW-01", "FI-SW-02");
        }

        @Test
        @DisplayName("a product carries its owning category id and localized name/description")
        void productIsFullyResolved() {
            Product angelfish = catalog.findProduct("FI-SW-01", EN).orElseThrow();

            assertThat(angelfish.categoryId()).isEqualTo("FISH");
            assertThat(angelfish.name()).isEqualTo("Angelfish");
            assertThat(angelfish.description()).isEqualTo("Salt Water fish from Australia");

            assertThat(catalog.findProduct("FI-SW-01", JA)).map(Product::name)
                    .contains("エンゼルフィッシュ"); // エンゼルフィッシュ
        }
    }

    @Nested
    @DisplayName("items")
    class Items {

        @Test
        @DisplayName("a product's items resolve with price and positional attributes")
        void angelfishItemsAndAttributes() {
            List<Item> items = catalog.findItemsByProduct("FI-SW-01", EN);
            assertThat(items).extracting(Item::id).containsExactly("EST-1", "EST-2");

            Item est1 = catalog.findItem("EST-1", EN).orElseThrow();
            assertThat(est1.productId()).isEqualTo("FI-SW-01");
            assertThat(est1.listPrice()).isEqualByComparingTo("16.50");
            assertThat(est1.unitCost()).isEqualByComparingTo("10.00");
            // attr1..attr5 collapse to the non-blank attributes, in positional order.
            assertThat(est1.attributes()).containsExactly("Large", "Cuddly");

            // EST-2 has only attr1 populated in the legacy data.
            assertThat(catalog.findItem("EST-2", EN)).map(Item::attributes)
                    .contains(List.of("Small"));
        }

        @Test
        @DisplayName("item pricing and description are per-locale (legacy stored localized prices)")
        void itemPricingIsLocalized() {
            Item est1Ja = catalog.findItem("EST-1", JA).orElseThrow();
            assertThat(est1Ja.listPrice()).isEqualByComparingTo(new BigDecimal("1951"));
            assertThat(est1Ja.description()).isEqualTo("日本産の淡水魚"); // 日本産の淡水魚
        }

        @Test
        @DisplayName("an unknown item is absent")
        void missingItemIsEmpty() {
            assertThat(catalog.findItem("EST-999", EN)).isEmpty();
        }
    }

    @Nested
    @DisplayName("catalog totals (whole-seed parity)")
    class Totals {

        @Test
        @DisplayName("the en_US catalog reproduces the legacy row counts across the hierarchy")
        void legacyRowCounts() {
            List<Category> categories = catalog.findCategories(EN);
            assertThat(categories).hasSize(5);

            long products = categories.stream()
                    .flatMap(c -> catalog.findProductsByCategory(c.id(), EN).stream())
                    .count();
            assertThat(products).isEqualTo(16);

            long items = categories.stream()
                    .flatMap(c -> catalog.findProductsByCategory(c.id(), EN).stream())
                    .flatMap(p -> catalog.findItemsByProduct(p.id(), EN).stream())
                    .count();
            assertThat(items).isEqualTo(28);
        }
    }

    @Test
    @DisplayName("every product resolves in all three seeded locales")
    void allLocalesResolveEveryProduct() {
        List<String> categoryIds = catalog.findCategories(EN).stream().map(Category::id).toList();
        for (Locale locale : List.of(EN, JA, ZH)) {
            long products = categoryIds.stream()
                    .flatMap(catid -> catalog.findProductsByCategory(catid, locale).stream())
                    .map(Product::id)
                    .distinct()
                    .count();
            assertThat(products)
                    .as("distinct products resolved in %s", locale)
                    .isEqualTo(16);
        }
    }

    @Test
    @DisplayName("a bogus item id yields no attributes and no crash")
    void bogusLookupsAreSafe() {
        Optional<Item> missing = catalog.findItem("DOES-NOT-EXIST", EN);
        assertThat(missing).isEmpty();
        assertThat(catalog.findItemsByProduct("NO-SUCH-PRODUCT", EN)).isEmpty();
    }
}
