package com.example.petstore.order.adapter.out.submission;

import com.example.petstore.order.application.port.out.OrderSubmissionPort;
import com.example.petstore.order.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default {@link OrderSubmissionPort} adapter: records the hand-off to the Order Processing
 * Center. This is the faithful, broker-free stand-in for the legacy asynchronous JMS producer
 * (ADR-0006) — the anti-corruption boundary is real and visible, but no external OPC is required.
 * A real JMS/Kafka/HTTP adapter can replace this with zero change to {@code CheckoutService}.
 */
@Component
public class LoggingOrderSubmissionAdapter implements OrderSubmissionPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingOrderSubmissionAdapter.class);

    @Override
    public void submit(Order order) {
        log.info("Submitting order {} to OPC seam: {} line(s), total {}, for user '{}'",
                order.orderId(), order.lines().size(), order.total(), order.username());
    }
}
