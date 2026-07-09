package com.atamanahmet.vinylexchange.service.order;

import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.service.shipment.ShipmentService;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderStatusHistory;
import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.domain.enums.AddressType;
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
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import com.atamanahmet.vinylexchange.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock private ShipmentService shipmentService;
    @Mock private UserAddressService userAddressService;
    @Mock private UserService userService;
    @Mock private ListingService listingService;

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
        when(userService.findUsernamesByIds(any())).thenReturn(Map.of());
        when(listingService.getListingsByIds(any())).thenReturn(Collections.emptyList());
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
     * Buyer confirms delivery, SHIPPED to DELIVERED, event published
     */
    @Test
    void confirmDelivery_shippedOrder_transitionsToDelivered() {
        order.setStatus(OrderStatus.SHIPPED);
        order.setOrderItems(new ArrayList<>());

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

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

    /** Shipment webhook reports shipment, PAID to SHIPPED, tracking number set */
    @Test
    void markShipped_paidOrder_transitionsToShippedAndSetsTracking() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByShipmentOrderId("MOCK-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.markShipped("MOCK-001", "1234567890", "TRACK-001");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getShipmentTrackingNumber()).isEqualTo("TRACK-001");
        assertThat(order.getAutoConfirmDeadline()).isNotNull();

        verify(eventPublisher).publishEvent(any(OrderShippedEvent.class));
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /** markShipped on non-PAID order must reject, webhook already processed */
    @Test
    void markShipped_wrongStatus_throwsInvalidTransition() {
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findByShipmentOrderId("MOCK-001")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markShipped("MOCK-001", "1234567890", "TRACK-001"))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(orderRepository, never()).save(any());
    }

    /** Shipment webhook reports out for delivery, SHIPPED to OUT_FOR_DELIVERY, no event */
    @Test
    void markOutForDelivery_shippedOrder_transitionsToOutForDelivery() {
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findByShipmentOrderId("MOCK-002")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.markOutForDelivery("MOCK-002");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);

        verify(eventPublisher, never()).publishEvent(any());
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /** markOutForDelivery on non-SHIPPED order must reject */
    @Test
    void markOutForDelivery_wrongStatus_throwsInvalidTransition() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByShipmentOrderId("MOCK-002")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markOutForDelivery("MOCK-002"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    /** Shipment webhook reports delivery, OUT_FOR_DELIVERY to DELIVERED, timestamps set */
    @Test
    void markDelivered_outForDeliveryOrder_transitionsToDelivered() {
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        when(orderRepository.findByShipmentOrderId("MOCK-002")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.markDelivered("MOCK-002");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getDeliveredAt()).isNotNull();

        verify(eventPublisher).publishEvent(any(OrderDeliveredEvent.class));
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    /** markDelivered on non-OUT_FOR_DELIVERY order must reject */
    @Test
    void markDelivered_wrongStatus_throwsInvalidTransition() {
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findByShipmentOrderId("MOCK-002")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markDelivered("MOCK-002"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    /** Seller generates label on PAID order, shipment fields saved, history recorded */
    @Test
    void generateShipmentLabel_paidOrderValidSeller_savesShipmentFields() {
        order.setStatus(OrderStatus.PAID);
        UUID sellerAddressId = UUID.randomUUID();

        UserAddress sellerAddress = UserAddress.builder()
                .id(sellerAddressId)
                .userId(sellerId)
                .label("Warehouse")
                .fullName("Seller User")
                .phone("+905559998877")
                .addressLine("Seller Street 5")
                .district("Cankaya")
                .city("Ankara")
                .postalCode("06000")
                .country("TR")
                .addressType(AddressType.SHIPPING)
                .build();

        when(userAddressService.getAddressOrThrow(sellerId, sellerAddressId)).thenReturn(sellerAddress);
        when(shipmentService.createShipmentForOrder(eq(order), eq("ARAS"), eq(sellerAddress)))
                .thenAnswer(invocation -> {
                    Order shipmentOrder = invocation.getArgument(0);
                    shipmentOrder.setShipmentOrderId("MOCK-LABEL-001");
                    return shipmentOrder;
                });
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.generateShipmentLabel(orderId, sellerId, "ARAS", sellerAddressId);

        assertThat(result.getShipmentOrderId()).isEqualTo("MOCK-LABEL-001");

        ArgumentCaptor<OrderStatusHistory> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getNote()).contains("Shipment label generated");
    }

    /** Another seller cannot generate label for order they do not own */
    @Test
    void generateShipmentLabel_wrongSeller_throwsAccessDenied() {
        order.setStatus(OrderStatus.PAID);
        UUID otherSeller = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.generateShipmentLabel(
                orderId, otherSeller, "ARAS", UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    /** Label generation only allowed on PAID orders */
    @Test
    void generateShipmentLabel_wrongStatus_throwsIllegalState() {
        order.setStatus(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> orderService.generateShipmentLabel(
                orderId, sellerId, "ARAS", UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Seller must pick a SHIPPING type address, not BILLING */
    @Test
    void generateShipmentLabel_nonShippingAddress_throwsIllegalArgument() {
        order.setStatus(OrderStatus.PAID);
        UUID sellerAddressId = UUID.randomUUID();

        UserAddress billingAddress = UserAddress.builder()
                .id(sellerAddressId)
                .userId(sellerId)
                .label("Billing")
                .fullName("Seller User")
                .phone("+905559998877")
                .addressLine("Billing Street 1")
                .district("Cankaya")
                .city("Ankara")
                .postalCode("06000")
                .country("TR")
                .addressType(AddressType.BILLING)
                .build();

        when(userAddressService.getAddressOrThrow(sellerId, sellerAddressId)).thenReturn(billingAddress);

        assertThatThrownBy(() -> orderService.generateShipmentLabel(
                orderId, sellerId, "ARAS", sellerAddressId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}