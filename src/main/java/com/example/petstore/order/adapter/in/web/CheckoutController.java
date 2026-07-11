package com.example.petstore.order.adapter.in.web;

import com.example.petstore.cart.application.port.in.ManageCart;
import com.example.petstore.order.application.port.in.OrderHistory;
import com.example.petstore.order.application.port.in.PlaceOrder;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.ShippingDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Locale;

/**
 * Inbound web adapter for checkout and a user's orders. All routes here require authentication
 * (enforced by {@code SecurityConfig}'s {@code anyRequest().authenticated()}), so a
 * {@link Principal} is always present.
 */
@Controller
public class CheckoutController {

    private final PlaceOrder placeOrder;
    private final OrderHistory orderHistory;
    private final ManageCart cart;

    public CheckoutController(PlaceOrder placeOrder, OrderHistory orderHistory, ManageCart cart) {
        this.placeOrder = placeOrder;
        this.orderHistory = orderHistory;
        this.cart = cart;
    }

    @GetMapping("/checkout")
    public String checkout(Locale locale, Model model) {
        if (cart.view(locale).isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart.view(locale));
        model.addAttribute("form", new ShippingForm());
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String place(@Valid @ModelAttribute("form") ShippingForm form, BindingResult binding,
                        Principal principal, Locale locale, Model model) {
        if (cart.view(locale).isEmpty()) {
            return "redirect:/cart";
        }
        if (binding.hasErrors()) {
            model.addAttribute("cart", cart.view(locale));
            return "order/checkout";
        }
        ShippingDetails shipping = new ShippingDetails(
                form.getName(), form.getAddressLine(), form.getCity(), form.getEmail());
        Order order = placeOrder.place(principal.getName(), shipping, locale);
        return "redirect:/orders/" + order.orderId();
    }

    @GetMapping("/orders/{orderId}")
    public String confirmation(@PathVariable String orderId, Principal principal, Model model) {
        Order order = orderHistory.find(orderId, principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such order"));
        model.addAttribute("order", order);
        return "order/confirmation";
    }

    @GetMapping("/orders")
    public String history(Principal principal, Model model) {
        model.addAttribute("orders", orderHistory.forUser(principal.getName()));
        return "order/orders";
    }
}
