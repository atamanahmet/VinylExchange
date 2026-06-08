package com.atamanahmet.vinylexchange.event.listener;

import com.atamanahmet.vinylexchange.domain.NotificationCommand;
import com.atamanahmet.vinylexchange.domain.enums.NotificationType;
import com.atamanahmet.vinylexchange.event.DisputeOpenedEvent;
import com.atamanahmet.vinylexchange.event.DisputeResolvedEvent;
import com.atamanahmet.vinylexchange.event.PaymentStateChangedEvent;
import com.atamanahmet.vinylexchange.event.OrderCancelledEvent;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.service.NotificationService;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationListener.class);

    private final NotificationService notificationService;
    private final OrderService orderService;


    /**
     * Payment-level notifications only  HELD, RELEASED, REFUNDED
     * Shipping and dispute notifications handled by order event listeners below
     */
    @Async
    @EventListener
    public void onPaymentStateChanged(PaymentStateChangedEvent event) {
        try {
            NotificationCommand command = switch (event.newStatus()) {
                case HELD -> new NotificationCommand(
                        NotificationType.ORDER,
                        "Payment Secured",
                        "Your payment is held securely. Seller will ship within 5 days.",
                         null);
                case RELEASED -> new NotificationCommand(
                        NotificationType.ORDER,
                        "Payment Released",
                        "Payment released. Transaction complete.",
                         null);
                case REFUNDED -> new NotificationCommand(
                        NotificationType.ORDER,
                        "Refund Issued",
                        "Your payment has been refunded.",
                         null);
                default -> null;
            };

            if (command == null) return;

            List<UUID> recipients = switch (event.newStatus()) {
                case HELD, RELEASED -> List.of(event.buyerId(), event.sellerId());
                case REFUNDED -> List.of(event.buyerId());
                default -> List.of();
            };

            if (!recipients.isEmpty()) {
                notificationService.notifyUsers(recipients, command);
            }

        } catch (Exception e) {
            logger.error("Payment notification failed for order {}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    /** Seller shipped, notify buyer */
    @Async
    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        try {
            var order = orderService.getOrderById(event.orderId());
            notificationService.notifyUsers(
                    List.of(order.getBuyerId()),
                    new NotificationCommand(
                            NotificationType.ORDER,
                            "Your Order Has Shipped",
                            "Seller marked your order as shipped. Confirm delivery when it arrives.",
                             null));
        } catch (Exception e) {
            logger.error("Ship notification failed for order {}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    /** Dispute opened, notify both parties */
    @Async
    @EventListener
    public void onDisputeOpened(DisputeOpenedEvent event) {
        try {
            var order = orderService.getOrderById(event.orderId());
            notificationService.notifyUsers(
                    List.of(order.getBuyerId(), order.getSellerId()),
                    new NotificationCommand(
                            NotificationType.ORDER,
                            "Dispute Opened",
                            "A dispute has been opened on order #" + event.orderId() + ".",
                             null));
        } catch (Exception e) {
            logger.error("Dispute notification failed for order {}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    /** Dispute resolved, notify both parties */
    @Async
    @EventListener
    public void onDisputeResolved(DisputeResolvedEvent event) {
        try {
            var order = orderService.getOrderById(event.orderId());
            String message = event.resolution() == DisputeResolvedEvent.Resolution.FOR_SELLER
                    ? "Dispute resolved in seller's favor. Payment released."
                    : "Dispute resolved in buyer's favor. Refund issued.";
            notificationService.notifyUsers(
                    List.of(order.getBuyerId(), order.getSellerId()),
                    new NotificationCommand(
                            NotificationType.ORDER,
                            "Dispute Resolved", message,
                             null));
        } catch (Exception e) {
            logger.error("Dispute resolved notification failed for order {}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    /** Order cancelled, notify seller */
    @Async
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        try {
            var order = orderService.getOrderById(event.orderId());
            notificationService.notifyUsers(
                    List.of(order.getSellerId()),
                    new NotificationCommand(
                            NotificationType.ORDER,
                            "Order Cancelled",
                            "Buyer cancelled order #" + event.orderId() + ". Refund in progress.",
                             null));
        } catch (Exception e) {
            logger.error("Cancel notification failed for order {}: {}",
                    event.orderId(), e.getMessage());
        }
    }
}