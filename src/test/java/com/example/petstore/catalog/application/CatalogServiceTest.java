package com.example.petstore.catalog.application;

import com.example.petstore.catalog.application.port.CatalogRepository;
import com.example.petstore.catalog.domain.Category;
import com.example.petstore.catalog.domain.Item;
import com.example.petstore.catalog.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the catalog use case: verifies it composes the hierarchy from the outbound port
 * and maps absence to {@link Optional#empty()} without touching the child lookups.
 */
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    private static final Locale EN = Locale.of("en", "US");

    @Mock
    private CatalogRepository repository;

    @InjectMocks
    private CatalogService service;

    @Test
    void viewCategoryComposesCategoryWithItsProducts() {
        var fish = new Category("FISH", "Fish", "fish_icon.gif");
        var angelfish = new Product("FI-SW-01", "FISH", "Angelfish", "Salt Water fish", "fish1.jpg");
        when(repository.findCategory("FISH", EN)).thenReturn(Optional.of(fish));
        when(repository.findProductsByCategory("FISH", EN)).thenReturn(List.of(angelfish));

        Optional<CategoryPage> page = service.viewCategory("FISH", EN);

        assertThat(page).isPresent();
        assertThat(page.get().category()).isEqualTo(fish);
        assertThat(page.get().products()).containsExactly(angelfish);
    }

    @Test
    void viewCategoryReturnsEmptyAndSkipsProductLookupWhenCategoryMissing() {
        when(repository.findCategory("NOPE", EN)).thenReturn(Optional.empty());

        assertThat(service.viewCategory("NOPE", EN)).isEmpty();
        verify(repository, never()).findProductsByCategory("NOPE", EN);
    }

    @Test
    void viewProductComposesProductWithItsItems() {
        var angelfish = new Product("FI-SW-01", "FISH", "Angelfish", "Salt Water fish", "fish1.jpg");
        var est1 = new Item("EST-1", "FI-SW-01", new BigDecimal("16.50"), new BigDecimal("10.00"),
                "fish3.gif", "Large Angelfish", List.of("Large"));
        when(repository.findProduct("FI-SW-01", EN)).thenReturn(Optional.of(angelfish));
        when(repository.findItemsByProduct("FI-SW-01", EN)).thenReturn(List.of(est1));

        Optional<ProductPage> page = service.viewProduct("FI-SW-01", EN);

        assertThat(page).isPresent();
        assertThat(page.get().product()).isEqualTo(angelfish);
        assertThat(page.get().items()).containsExactly(est1);
    }

    @Test
    void listCategoriesAndViewItemDelegateToTheRepository() {
        var fish = new Category("FISH", "Fish", "fish_icon.gif");
        when(repository.findCategories(EN)).thenReturn(List.of(fish));
        assertThat(service.listCategories(EN)).containsExactly(fish);

        when(repository.findItem("EST-1", EN)).thenReturn(Optional.empty());
        assertThat(service.viewItem("EST-1", EN)).isEmpty();
    }
}
