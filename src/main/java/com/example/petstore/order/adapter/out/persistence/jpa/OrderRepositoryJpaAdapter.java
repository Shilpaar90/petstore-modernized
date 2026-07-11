package com.example.petstore.order.adapter.out.persistence.jpa;

import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.OrderLine;
import com.example.petstore.order.domain.ShippingDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Relational (JPA) implementation of the {@link OrderRepository} port, translating between the
 * domain {@link Order} aggregate and the {@code orders}/{@code order_lines} entities.
 */
@Repository
@Profile("!mongo")
public class OrderRepositoryJpaAdapter implements OrderRepository {

    private final OrderJpaRepository orders;

    public OrderRepositoryJpaAdapter(OrderJpaRepository orders) {
        this.orders = orders;
    }

    @Override
    public void save(Order order) {
        OrderEntity entity = new OrderEntity(
                order.orderId(), order.username(), order.total(), order.status(), order.placedAt(),
                order.shipping().name(), order.shipping().addressLine(),
                order.shipping().city(), order.shipping().email());
        for (OrderLine line : order.lines()) {
            entity.addLine(new OrderLineEntity(
                    line.itemId(), line.description(), line.unitPrice(), line.quantity(), line.lineTotal()));
        }
        orders.save(entity);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return orders.findById(orderId).map(this::toDomain);
    }

    @Override
    public List<Order> findByUsername(String username) {
        return orders.findByUsernameOrderByPlacedAtDesc(username).stream().map(this::toDomain).toList();
    }

    private Order toDomain(OrderEntity e) {
        List<OrderLine> lines = e.getLines().stream()
                .map(l -> new OrderLine(l.getItemId(), l.getDescription(), l.getUnitPrice(),
                        l.getQuantity(), l.getLineTotal()))
                .toList();
        return new Order(e.getOrderId(), e.getUsername(), lines, e.getTotal(), e.getStatus(), e.getPlacedAt(),
                new ShippingDetails(e.getShipName(), e.getShipAddress(), e.getShipCity(), e.getShipEmail()));
    }
}
