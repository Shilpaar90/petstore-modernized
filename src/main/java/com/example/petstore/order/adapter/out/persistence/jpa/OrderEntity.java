package com.example.petstore.order.adapter.out.persistence.jpa;

import com.example.petstore.order.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps the {@code orders} table (Flyway V4), owning its {@code order_lines} by composition.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "username")
    private String username;

    @Column(name = "total")
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "ship_name")
    private String shipName;

    @Column(name = "ship_address")
    private String shipAddress;

    @Column(name = "ship_city")
    private String shipCity;

    @Column(name = "ship_email")
    private String shipEmail;

    // An order aggregate always wants its lines; eager keeps the mapping session-independent.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderLineEntity> lines = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(String orderId, String username, BigDecimal total, OrderStatus status, Instant placedAt,
                       String shipName, String shipAddress, String shipCity, String shipEmail) {
        this.orderId = orderId;
        this.username = username;
        this.total = total;
        this.status = status;
        this.placedAt = placedAt;
        this.shipName = shipName;
        this.shipAddress = shipAddress;
        this.shipCity = shipCity;
        this.shipEmail = shipEmail;
    }

    public void addLine(OrderLineEntity line) {
        line.setOrder(this);
        this.lines.add(line);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public String getShipName() {
        return shipName;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public String getShipCity() {
        return shipCity;
    }

    public String getShipEmail() {
        return shipEmail;
    }

    public List<OrderLineEntity> getLines() {
        return lines;
    }
}
