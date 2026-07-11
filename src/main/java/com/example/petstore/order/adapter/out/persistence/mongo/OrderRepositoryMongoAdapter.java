package com.example.petstore.order.adapter.out.persistence.mongo;

import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.OrderLine;
import com.example.petstore.order.domain.ShippingDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation of the {@link OrderRepository} port ({@code mongo} profile). Maps the
 * domain {@link Order} aggregate to a single self-contained document (lines + shipping embedded).
 */
@Repository
@Profile("mongo")
public class OrderRepositoryMongoAdapter implements OrderRepository {

    private final OrderDocumentRepository orders;

    public OrderRepositoryMongoAdapter(OrderDocumentRepository orders) {
        this.orders = orders;
    }

    @Override
    public void save(Order order) {
        List<OrderDocument.Line> lines = order.lines().stream()
                .map(l -> new OrderDocument.Line(l.itemId(), l.description(), l.unitPrice(), l.quantity(), l.lineTotal()))
                .toList();
        OrderDocument.Shipping shipping = new OrderDocument.Shipping(
                order.shipping().name(), order.shipping().addressLine(),
                order.shipping().city(), order.shipping().email());
        orders.save(new OrderDocument(
                order.orderId(), order.username(), order.total(), order.status(), order.placedAt(), lines, shipping));
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return orders.findById(orderId).map(this::toDomain);
    }

    @Override
    public List<Order> findByUsername(String username) {
        return orders.findByUsernameOrderByPlacedAtDesc(username).stream().map(this::toDomain).toList();
    }

    private Order toDomain(OrderDocument d) {
        List<OrderLine> lines = d.getLines().stream()
                .map(l -> new OrderLine(l.getItemId(), l.getDescription(), l.getUnitPrice(),
                        l.getQuantity(), l.getLineTotal()))
                .toList();
        OrderDocument.Shipping s = d.getShipping();
        return new Order(d.getOrderId(), d.getUsername(), lines, d.getTotal(), d.getStatus(), d.getPlacedAt(),
                new ShippingDetails(s.getName(), s.getAddressLine(), s.getCity(), s.getEmail()));
    }
}
