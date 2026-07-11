package com.example.petstore.order.application;

import com.example.petstore.cart.application.CartView;
import com.example.petstore.cart.application.port.in.ManageCart;
import com.example.petstore.order.application.port.in.OrderHistory;
import com.example.petstore.order.application.port.in.PlaceOrder;
import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.application.port.out.OrderSubmissionPort;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.OrderLine;
import com.example.petstore.order.domain.OrderStatus;
import com.example.petstore.order.domain.ShippingDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * The checkout use case. Snapshots the current (priced) cart into a durable {@link Order},
 * persists it, hands it to the {@link OrderSubmissionPort} OPC seam, then clears the cart — the
 * modernized replacement for the legacy "serialize to XML and drop on a JMS queue" flow
 * (ADR-0006). Also serves a user's own order reads.
 */
@Service
public class CheckoutService implements PlaceOrder, OrderHistory {

    private final ManageCart cart;
    private final OrderRepository orders;
    private final OrderSubmissionPort orderSubmission;

    public CheckoutService(ManageCart cart, OrderRepository orders, OrderSubmissionPort orderSubmission) {
        this.cart = cart;
        this.orders = orders;
        this.orderSubmission = orderSubmission;
    }

    @Override
    @Transactional
    public Order place(String username, ShippingDetails shipping, Locale locale) {
        CartView view = cart.view(locale);
        if (view.isEmpty()) {
            throw new EmptyCartException();
        }

        List<OrderLine> lines = view.lines().stream()
                .map(l -> new OrderLine(l.itemId(), l.description(), l.unitPrice(), l.quantity(), l.lineTotal()))
                .toList();

        Order order = new Order(
                UUID.randomUUID().toString(),
                username,
                lines,
                view.total(),
                OrderStatus.SUBMITTED,
                Instant.now(),
                shipping);

        orders.save(order);          // durable record first ...
        orderSubmission.submit(order); // ... then hand off to the OPC seam
        cart.clear();
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> forUser(String username) {
        return orders.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> find(String orderId, String username) {
        return orders.findByOrderId(orderId)
                .filter(o -> o.username().equals(username));
    }
}
