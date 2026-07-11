package com.example.petstore.order.application;

import com.example.petstore.cart.application.CartLine;
import com.example.petstore.cart.application.CartView;
import com.example.petstore.cart.application.port.in.ManageCart;
import com.example.petstore.order.application.port.out.OrderRepository;
import com.example.petstore.order.application.port.out.OrderSubmissionPort;
import com.example.petstore.order.domain.Order;
import com.example.petstore.order.domain.OrderStatus;
import com.example.petstore.order.domain.ShippingDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final Locale EN = Locale.of("en", "US");
    private static final ShippingDetails SHIP =
            new ShippingDetails("Alice", "1 Main St", "Springfield", "alice@example.com");

    @Mock
    private ManageCart cart;
    @Mock
    private OrderRepository orders;
    @Mock
    private OrderSubmissionPort submission;

    private CheckoutService service() {
        return new CheckoutService(cart, orders, submission);
    }

    @Test
    void placeSnapshotsCartPersistsSubmitsThenClears() {
        CartView view = new CartView(List.of(
                new CartLine("EST-1", "Large Angelfish", new BigDecimal("16.50"), 2, new BigDecimal("33.00"))),
                new BigDecimal("33.00"), 2);
        when(cart.view(EN)).thenReturn(view);

        Order order = service().place("alice", SHIP, EN);

        assertThat(order.orderId()).isNotBlank();
        assertThat(order.username()).isEqualTo("alice");
        assertThat(order.status()).isEqualTo(OrderStatus.SUBMITTED);
        assertThat(order.total()).isEqualByComparingTo("33.00");
        assertThat(order.lines()).singleElement()
                .satisfies(l -> {
                    assertThat(l.itemId()).isEqualTo("EST-1");
                    assertThat(l.lineTotal()).isEqualByComparingTo("33.00");
                });
        assertThat(order.shipping()).isEqualTo(SHIP);

        // Persist BEFORE submitting to the OPC seam, then clear the cart.
        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        InOrder ordered = inOrder(orders, submission, cart);
        ordered.verify(orders).save(saved.capture());
        ordered.verify(submission).submit(saved.getValue());
        ordered.verify(cart).clear();
        assertThat(saved.getValue().orderId()).isEqualTo(order.orderId());
    }

    @Test
    void placeWithEmptyCartThrowsAndTouchesNothing() {
        when(cart.view(EN)).thenReturn(new CartView(List.of(), BigDecimal.ZERO, 0));

        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> service().place("alice", SHIP, EN));

        verifyNoInteractions(orders, submission);
        verify(cart, never()).clear();
    }

    @Test
    void findReturnsOrderOnlyForItsOwner() {
        Order order = new Order("o-1", "alice", List.of(), new BigDecimal("1.00"),
                OrderStatus.SUBMITTED, java.time.Instant.EPOCH, SHIP);
        when(orders.findByOrderId("o-1")).thenReturn(Optional.of(order));

        CheckoutService service = service();
        assertThat(service.find("o-1", "alice")).contains(order);
        assertThat(service.find("o-1", "mallory")).isEmpty(); // not your order
    }
}
