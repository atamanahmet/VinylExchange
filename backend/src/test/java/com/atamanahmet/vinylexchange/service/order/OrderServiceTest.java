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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private OrderService orderService;

    private UUID orderId;
    private UUID buyerId;
    private UUID sellerId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId  = UUID.randomUUID();
        buyerId  = UUID.randomUUID();
        sellerId = UUID.randomUUID();

        order = Order.builder()
                .id(orderId)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    }

    /**
     * Payment captured, AWAITING_PAYMENT to PAID
     */
    @Test
    void markPaid_awaitingPayment_transitionsToPaid() {
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.markPaid(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);

        assertThat(result.getPaidAt()).isNotNull();

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * markPaid called on already paid order, must reject
     */
    @Test
    void markPaid_wrongStatus_throwsInvalidTransition() {
        order.setStatus(OrderStatus.PAID);

        assertThatThrownBy(() -> orderService.markPaid(orderId))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(orderStatusHistoryRepository, never()).save(any());
    }

    /**
     * Buyer cancels before shipment, PAID to CANCELLED, event published
     */
    @Test
    void cancelOrder_paidOrder_transitionsToCancelled() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancelOrder(orderId, buyerId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().orderId()).isEqualTo(orderId);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * Buyer cancels after shipment, must be rejected, funds in escrow already
     */
    @Test
    void cancelOrder_shippedOrder_throwsInvalidTransition() {
        order.setStatus(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, buyerId))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(eventPublisher, never()).publishEvent(any());

        verify(orderStatusHistoryRepository, never()).save(any());
    }

    /**
     * Wrong user tries to cancel someone else's order
     */
    @Test
    void cancelOrder_wrongBuyer_throwsUnauthorized() {
        order.setStatus(OrderStatus.PAID);
        UUID otherUser = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, otherUser))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    /**
     * Funds released after delivery, DELIVERED to COMPLETED
     */
    @Test
    void completeOrder_deliveredOrder_transitionsToCompleted() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.completeOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * Dispute resolved for seller, DISPUTED to COMPLETED must also be valid
     */
    @Test
    void completeOrder_disputedOrder_transitionsToCompleted() {
        order.setStatus(OrderStatus.DISPUTED);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.completeOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * completeOrder called from invalid state, must reject
     */
    @Test
    void completeOrder_wrongStatus_throwsInvalidTransition() {
        order.setStatus(OrderStatus.PAID);

        assertThatThrownBy(() -> orderService.completeOrder(orderId))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(orderStatusHistoryRepository, never()).save(any());
    }

    /**
     * Dispute resolved for buyer, DISPUTED to REFUNDED
     */
    @Test
    void refundOrder_disputedOrder_transitionsToRefunded() {
        order.setStatus(OrderStatus.DISPUTED);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.refundOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.REFUNDED);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * Canceled order refunded, CANCELLED to REFUNDED
     */
    @Test
    void refundOrder_cancelledOrder_transitionsToRefunded() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.refundOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.REFUNDED);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * Buyer disputes within 14 days, DELIVERED to DISPUTED, event published
     */
    @Test
    void openDispute_withinWindow_transitionsToDisputed() {
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now().minusDays(3));

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.openDispute(orderId, buyerId, DisputeReason.ITEM_NOT_RECEIVED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DISPUTED);

        ArgumentCaptor<DisputeOpenedEvent> captor = ArgumentCaptor.forClass(DisputeOpenedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().orderId()).isEqualTo(orderId);

        assertThat(captor.getValue().reason()).isEqualTo(DisputeReason.ITEM_NOT_RECEIVED);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * Buyer disputes after 14-day window, must be rejected
     */
    @Test
    void openDispute_pastWindow_throwsInvalidOperation() {
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now().minusDays(15));

        assertThatThrownBy(() -> orderService.openDispute(orderId, buyerId, DisputeReason.ITEM_NOT_RECEIVED))
                .isInstanceOf(InvalidOrderOperationException.class)
                .hasMessageContaining("14 days");

        verify(eventPublisher, never()).publishEvent(any());

        verify(orderStatusHistoryRepository, never()).save(any());
    }

    /**
     * Seller ships, PAID to SHIPPED, autoConfirmDeadline set, event published
     */
    @Test
    void shipOrder_paidOrder_transitionsToShipped() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.shipOrder(orderId, sellerId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        assertThat(order.getAutoConfirmDeadline()).isNotNull();

        verify(eventPublisher).publishEvent(any(OrderShippedEvent.class));

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * Wrong seller tries to ship someone else's order
     */
    @Test
    void shipOrder_wrongSeller_throwsUnauthorized() {
        order.setStatus(OrderStatus.PAID);
        UUID otherSeller = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.shipOrder(orderId, otherSeller))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    /**
     * Buyer confirms delivery, SHIPPED to DELIVERED, event published
     */
    @Test
    void confirmDelivery_shippedOrder_transitionsToDelivered() {
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.confirmDelivery(orderId, buyerId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        assertThat(order.getDeliveredAt()).isNotNull();

        verify(eventPublisher).publishEvent(any(OrderDeliveredEvent.class));

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * autoConfirm fires, SHIPPED to DELIVERED without buyer action
     */
    @Test
    void autoConfirmDelivery_shippedOrder_transitionsToDelivered() {
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.autoConfirmDelivery(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        verify(eventPublisher).publishEvent(any(OrderDeliveredEvent.class));

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /**
     * autoConfirm fires but buyer already confirmed, must throw, scheduler catches it
     */
    @Test
    void autoConfirmDelivery_alreadyDelivered_throwsInvalidTransition() {
        order.setStatus(OrderStatus.DELIVERED);

        assertThatThrownBy(() -> orderService.autoConfirmDelivery(orderId))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(orderStatusHistoryRepository, never()).save(any());
    }
}