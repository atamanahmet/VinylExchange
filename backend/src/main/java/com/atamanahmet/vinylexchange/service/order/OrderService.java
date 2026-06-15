package com.atamanahmet.vinylexchange.service.order;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderStatusHistory;
import com.atamanahmet.vinylexchange.domain.enums.DisputeReason;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.event.DisputeOpenedEvent;
import com.atamanahmet.vinylexchange.event.OrderCancelledEvent;
import com.atamanahmet.vinylexchange.event.OrderDeliveredEvent;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.InvalidStatusTransitionException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.repository.order.OrderRepository;
import com.atamanahmet.vinylexchange.repository.order.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderOperationException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByBuyerId(UUID buyerId) {
        return orderRepository.findAllByBuyerId(buyerId);
    }

    public List<Order> getOrdersBySellerId(UUID sellerId) {
        return orderRepository.findAllBySellerId(sellerId);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Long getNextOrderNumber() {
        return orderRepository.getNextOrderNumber();
    }

    //
    // STATE TRANSITIONS
    //

    /**
     * Called by PaymentService after payment captured
     * AWAITING_PAYMENT to PAID
     */
    @Transactional
    public Order markPaid(UUID orderId) {
        Order order = getOrderById(orderId);
        assertStatus(order, OrderStatus.AWAITING_PAYMENT, "mark as paid");
        transition(order, OrderStatus.PAID, "Payment captured", "SYSTEM");
        order.setPaidAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Seller marks order as shipped
     * PAID to SHIPPED
     * Sets auto confirm deadline 3 days from now
     */
    @Transactional
    public Order shipOrder(UUID orderId, UUID sellerId) {
        Order order = getOrderById(orderId);
        validateSeller(order, sellerId);
        assertStatus(order, OrderStatus.PAID, "mark as shipped");
        transition(order, OrderStatus.SHIPPED, "Seller marked as shipped", sellerId.toString());
        order.setAutoConfirmDeadline(LocalDateTime.now().plusDays(3));
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderShippedEvent(orderId));
        return order;
    }

    /**
     * Buyer confirms delivery manually
     * SHIPPED to DELIVERED
     */
    @Transactional
    public Order confirmDelivery(UUID orderId, UUID buyerId) {
        Order order = getOrderById(orderId);
        validateBuyer(order, buyerId);
        assertStatus(order, OrderStatus.SHIPPED, "confirm delivery");
        transition(order, OrderStatus.DELIVERED, "Buyer confirmed delivery", buyerId.toString());
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderDeliveredEvent(orderId));
        return order;
    }

    /**
     * Scheduler calls this when autoConfirmDeadline passes, no buyer action
     * SHIPPED to DELIVERED
     */
    @Transactional
    public Order autoConfirmDelivery(UUID orderId) {
        Order order = getOrderById(orderId);
        assertStatus(order, OrderStatus.SHIPPED, "auto-confirm delivery");
        transition(order, OrderStatus.DELIVERED, "Auto-confirmed after deadline", "SCHEDULER");
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderDeliveredEvent(orderId));
        return order;
    }

    /**
     * Buyer cancels before shipment
     * AWAITING_PAYMENT or PAID to CANCELLED
     * Allowed by Turkish consumer law without seller approval
     */
    @Transactional
    public Order cancelOrder(UUID orderId, UUID buyerId) {
        Order order = getOrderById(orderId);
        validateBuyer(order, buyerId);
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT &&
                order.getStatus() != OrderStatus.PAID) {
            throw new InvalidStatusTransitionException(
                    "Cannot cancel order from status: " + order.getStatus());
        }
        transition(order, OrderStatus.CANCELLED, "Cancelled by buyer", buyerId.toString());
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCancelledEvent(orderId));
        return order;
    }

    /**
     * Scheduler cancels order when payment window expires
     * AWAITING_PAYMENT to CANCELLED
     */
    @Transactional
    public Order cancelExpiredOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        assertStatus(order, OrderStatus.AWAITING_PAYMENT, "cancel expired order");
        transition(order, OrderStatus.CANCELLED, "Payment window expired", "SCHEDULER");
        return orderRepository.save(order);
    }

    /**
     * Buyer opens dispute after delivery, only within 14 day window
     * DELIVERED to DISPUTED
     */
    @Transactional
    public Order openDispute(UUID orderId, UUID buyerId, DisputeReason reason) {
        Order order = getOrderById(orderId);
        validateBuyer(order, buyerId);
        assertStatus(order, OrderStatus.DELIVERED, "open dispute");
        assertWithinDisputeWindow(order);
        transition(order, OrderStatus.DISPUTED, "Dispute opened: " + reason, buyerId.toString());
        orderRepository.save(order);
        eventPublisher.publishEvent(new DisputeOpenedEvent(orderId, reason));
        return order;
    }

    /**
     * Called by PaymentService after funds released to seller
     * DELIVERED or DISPUTED to COMPLETED
     */
    @Transactional
    public Order completeOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED &&
                order.getStatus() != OrderStatus.DISPUTED) {
            throw new InvalidStatusTransitionException(
                    "Cannot complete order from status: " + order.getStatus());
        }
        transition(order, OrderStatus.COMPLETED, "Funds released to seller", "SYSTEM");
        return orderRepository.save(order);
    }

    /**
     * Called by PaymentService after refund issued to buyer
     * CANCELLED or DISPUTED to REFUNDED
     */
    @Transactional
    public Order refundOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != OrderStatus.CANCELLED &&
                order.getStatus() != OrderStatus.DISPUTED) {
            throw new InvalidStatusTransitionException(
                    "Cannot refund order from status: " + order.getStatus());
        }
        transition(order, OrderStatus.REFUNDED, "Refund issued to buyer", "SYSTEM");
        return orderRepository.save(order);
    }

    //
    // PRIVATE HELPERS
    //

    /**
     * Single place for all order status changes
     * Writes audit record to DB and structured log
     * triggeredBy is userId, SYSTEM, or SCHEDULER
     */
    private void transition(Order order, OrderStatus newStatus, String note, String triggeredBy) {
        OrderStatus previous = order.getStatus();
        order.setStatus(newStatus);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(previous)
                .toStatus(newStatus)
                .triggeredBy(triggeredBy)
                .occurredAt(LocalDateTime.now())
                .note(note)
                .build());

        log.info("event=ORDER_TRANSITION orderId={} from={} to={} triggeredBy={} note={}",
                order.getId(), previous, newStatus, triggeredBy, note);
    }

    private void assertStatus(Order order, OrderStatus expected, String action) {
        if (order.getStatus() != expected) {
            throw new InvalidStatusTransitionException(
                    "Cannot " + action + ", order is " + order.getStatus());
        }
    }

    /**
     * Dispute window is 14 days after delivery, Turkish consumer law requirement
     */
    private void assertWithinDisputeWindow(Order order) {
        if (order.getDeliveredAt() == null ||
                order.getDeliveredAt().plusDays(14).isBefore(LocalDateTime.now())) {
            throw new InvalidOrderOperationException(
                    "Dispute window has closed, 14 days have passed since delivery");
        }
    }

    private void validateBuyer(Order order, UUID userId) {
        if (!order.getBuyerId().equals(userId)) {
            throw new UnauthorizedActionException("User is not the buyer of this order");
        }
    }

    private void validateSeller(Order order, UUID userId) {
        if (!order.getSellerId().equals(userId)) {
            throw new UnauthorizedActionException("User is not the seller of this order");
        }
    }
}