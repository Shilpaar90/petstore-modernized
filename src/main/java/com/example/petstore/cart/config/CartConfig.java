package com.example.petstore.cart.config;

import com.example.petstore.cart.domain.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Wires the {@link Cart} as a session-scoped bean so each visitor has their own cart (the legacy
 * stateful session cart). Declared here — rather than annotating {@code Cart} — to keep the
 * domain aggregate free of Spring. The scoped proxy lets the singleton {@code CartService} hold a
 * reference that resolves to the current session's cart per request.
 */
@Configuration
public class CartConfig {

    @Bean
    @SessionScope
    public Cart sessionCart() {
        return new Cart();
    }
}
