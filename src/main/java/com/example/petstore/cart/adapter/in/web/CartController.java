package com.example.petstore.cart.adapter.in.web;

import com.example.petstore.cart.application.UnknownItemException;
import com.example.petstore.cart.application.port.in.ManageCart;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * Inbound web adapter for the session cart. Mutations are POST-then-redirect (PRG) so a refresh
 * doesn't re-submit. The cart is available to anonymous visitors (checkout auth arrives in
 * Phase 4).
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final ManageCart cart;

    public CartController(ManageCart cart) {
        this.cart = cart;
    }

    @GetMapping
    public String view(Locale locale, Model model) {
        model.addAttribute("cart", cart.view(locale));
        return "cart/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam String itemId,
                      @RequestParam(defaultValue = "1") int quantity,
                      Locale locale) {
        try {
            cart.add(itemId, quantity, locale);
        } catch (UnknownItemException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam String itemId, @RequestParam int quantity) {
        cart.updateQuantity(itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam String itemId) {
        cart.remove(itemId);
        return "redirect:/cart";
    }
}
