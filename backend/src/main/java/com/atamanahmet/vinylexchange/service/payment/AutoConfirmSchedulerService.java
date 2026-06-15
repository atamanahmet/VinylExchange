package com.atamanahmet.vinylexchange.service.payment;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

/**
 * Schedules auto-confirm exactly at autoConfirmDeadline per order.
 * No polling, fires once on time.
 *
 * Scale note: TaskScheduler works for single-instance deployments.
 * For horizontal scaling, TODO: replace with a delayed message queue (RabbitMQ TTL / Kafka).
 */
@Service
@RequiredArgsConstructor
public class AutoConfirmSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AutoConfirmSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final OrderService orderService;

    /**
     * Listens for shipment, schedules auto-confirm at order's autoConfirmDeadline
     * If cargo integration confirms delivery first, autoConfirmDelivery() status check handles duplicate safely
     */
    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        Order order = orderService.getOrderById(event.orderId());

        if (order.getAutoConfirmDeadline() == null) {
            logger.warn("No autoConfirmDeadline set for order={}, skipping scheduler", event.orderId());
            return;
        }

        taskScheduler.schedule(
                () -> autoConfirm(event.orderId()),
                order.getAutoConfirmDeadline().toInstant(ZoneOffset.UTC)
        );

        logger.info("Auto-confirm scheduled for order={} at {}", event.orderId(), order.getAutoConfirmDeadline());
    }

    private void autoConfirm(java.util.UUID orderId) {
        try {
            orderService.autoConfirmDelivery(orderId);
            logger.info("Auto-confirm fired for order={}", orderId);
        } catch (Exception e) {
            logger.error("Auto-confirm failed for order={}", orderId, e);
        }
    }
}