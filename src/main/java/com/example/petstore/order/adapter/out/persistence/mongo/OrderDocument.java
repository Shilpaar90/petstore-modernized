package com.example.petstore.order.adapter.out.persistence.mongo;

import com.example.petstore.order.domain.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * MongoDB projection of an order. Where the relational model splits into {@code orders} +
 * {@code order_lines}, the document model keeps the aggregate whole — lines and shipping are
 * embedded, so a placed order is a single atomic document (no multi-doc transaction needed).
 */
@Document(collection = "orders")
public class OrderDocument {

    /** Embedded priced line. */
    public static class Line {
        private String itemId;
        private String description;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal lineTotal;

        protected Line() {
        }

        public Line(String itemId, String description, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
            this.itemId = itemId;
            this.description = description;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
        }

        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }
    }

    /** Embedded shipping snapshot. */
    public static class Shipping {
        private String name;
        private String addressLine;
        private String city;
        private String email;

        protected Shipping() {
        }

        public Shipping(String name, String addressLine, String city, String email) {
            this.name = name;
            this.addressLine = addressLine;
            this.city = city;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getAddressLine() {
            return addressLine;
        }

        public String getCity() {
            return city;
        }

        public String getEmail() {
            return email;
        }
    }

    @Id
    private String orderId;
    private String username;
    private BigDecimal total;
    private OrderStatus status;
    private Instant placedAt;
    private List<Line> lines;
    private Shipping shipping;

    protected OrderDocument() {
    }

    public OrderDocument(String orderId, String username, BigDecimal total, OrderStatus status, Instant placedAt,
                         List<Line> lines, Shipping shipping) {
        this.orderId = orderId;
        this.username = username;
        this.total = total;
        this.status = status;
        this.placedAt = placedAt;
        this.lines = lines;
        this.shipping = shipping;
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

    public List<Line> getLines() {
        return lines == null ? List.of() : lines;
    }

    public Shipping getShipping() {
        return shipping;
    }
}
