package com.example.petstore.cart.application;

import com.example.petstore.cart.domain.Cart;
import com.example.petstore.catalog.application.port.in.CatalogQuery;
import com.example.petstore.catalog.domain.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the cart use case, over a real {@link Cart} and a mocked catalog port.
 */
class CartServiceTest {

    private static final Locale EN = Locale.of("en", "US");

    private final CatalogQuery catalog = mock(CatalogQuery.class);
    private Cart cart;
    private CartService service;

    private static Item item(String id, String price) {
        return new Item(id, "FI-SW-01", new BigDecimal(price), new BigDecimal("1.00"),
                "img.gif", "Large Angelfish", List.of("Large"));
    }

    @BeforeEach
    void setUp() {
        cart = new Cart();
        service = new CartService(cart, catalog);
    }

    @Test
    void addValidatesItemExistsAndStoresQuantity() {
        when(catalog.viewItem(eq("EST-1"), eq(EN))).thenReturn(Optional.of(item("EST-1", "16.50")));

        service.add("EST-1", 2, EN);

        assertThat(cart.quantities()).containsEntry("EST-1", 2);
    }

    @Test
    void addUnknownItemThrowsAndStoresNothing() {
        when(catalog.viewItem(eq("NOPE"), eq(EN))).thenReturn(Optional.empty());

        assertThatExceptionOfType(UnknownItemException.class)
                .isThrownBy(() -> service.add("NOPE", 1, EN));
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void viewPricesLinesAndRollsUpTheTotal() {
        cart.add("EST-1", 2);
        cart.add("EST-2", 1);
        when(catalog.viewItem(eq("EST-1"), eq(EN))).thenReturn(Optional.of(item("EST-1", "16.50")));
        when(catalog.viewItem(eq("EST-2"), eq(EN))).thenReturn(Optional.of(item("EST-2", "10.00")));

        CartView view = service.view(EN);

        assertThat(view.unitCount()).isEqualTo(3);
        assertThat(view.total()).isEqualByComparingTo("43.00"); // 2*16.50 + 1*10.00
        assertThat(view.lines()).hasSize(2);
        assertThat(view.lines().get(0).lineTotal()).isEqualByComparingTo("33.00");
    }

    @Test
    void viewSkipsItemsThatVanishedFromTheCatalog() {
        cart.add("GONE", 1);
        when(catalog.viewItem(eq("GONE"), eq(EN))).thenReturn(Optional.empty());

        CartView view = service.view(EN);

        assertThat(view.lines()).isEmpty();
        assertThat(view.total()).isEqualByComparingTo("0");
    }

    @Test
    void updateAndRemoveMutateTheCart() {
        cart.add("EST-1", 5);

        service.updateQuantity("EST-1", 2);
        assertThat(cart.quantities()).containsEntry("EST-1", 2);

        service.updateQuantity("EST-1", 0); // zero removes
        assertThat(cart.isEmpty()).isTrue();

        cart.add("EST-2", 1);
        service.remove("EST-2");
        assertThat(cart.isEmpty()).isTrue();
    }
}
