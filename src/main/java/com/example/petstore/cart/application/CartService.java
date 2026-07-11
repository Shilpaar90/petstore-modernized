package com.example.petstore.cart.application;

import com.example.petstore.cart.application.port.in.ManageCart;
import com.example.petstore.cart.domain.Cart;
import com.example.petstore.catalog.application.port.in.CatalogQuery;
import com.example.petstore.catalog.domain.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The shopping-cart use case. Holds only quantities (in the session-scoped {@link Cart}); prices
 * and descriptions are resolved from the catalog via {@link CatalogQuery} at add-time (to
 * validate the item exists) and at view-time (so the cart always shows current, localized prices).
 *
 * <p>This is a legitimate cross-context dependency: cart drives the catalog's inbound port, not
 * its internals.
 */
@Service
public class CartService implements ManageCart {

    private final Cart cart;
    private final CatalogQuery catalog;

    public CartService(Cart cart, CatalogQuery catalog) {
        this.cart = cart;
        this.catalog = catalog;
    }

    @Override
    public void add(String itemId, int quantity, Locale locale) {
        // Validate against the catalog so we never hold a phantom line.
        catalog.viewItem(itemId, locale).orElseThrow(() -> new UnknownItemException(itemId));
        cart.add(itemId, quantity);
    }

    @Override
    public void updateQuantity(String itemId, int quantity) {
        cart.setQuantity(itemId, quantity);
    }

    @Override
    public void remove(String itemId) {
        cart.remove(itemId);
    }

    @Override
    public void clear() {
        cart.clear();
    }

    @Override
    public CartView view(Locale locale) {
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : cart.quantities().entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            // An item that vanished from the catalog is skipped rather than crashing the page.
            Item item = catalog.viewItem(itemId, locale).orElse(null);
            if (item == null) {
                continue;
            }
            BigDecimal lineTotal = item.listPrice().multiply(BigDecimal.valueOf(quantity));
            lines.add(new CartLine(itemId, item.description(), item.listPrice(), quantity, lineTotal));
            total = total.add(lineTotal);
        }
        return new CartView(lines, total, cart.unitCount());
    }
}
