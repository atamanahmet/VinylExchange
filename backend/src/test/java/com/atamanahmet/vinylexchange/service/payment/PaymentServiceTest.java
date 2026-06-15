package com.atamanahmet.vinylexchange.service.payment;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.domain.enums.DisputeReason;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.dto.payment.PaymentVerifyResult;
import com.atamanahmet.vinylexchange.event.DisputeOpenedEvent;
import com.atamanahmet.vinylexchange.event.DisputeResolvedEvent;
import com.atamanahmet.vinylexchange.event.OrderCancelledEvent;
import com.atamanahmet.vinylexchange.event.OrderDeliveredEvent;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.event.PaymentStateChangedEvent;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.infrastructure.payment.IyzicoProperties;
import com.atamanahmet.vinylexchange.infrastructure.payment.PaymentPort;
import com.atamanahmet.vinylexchange.repository.payment.PaymentStatusHistoryRepository;
import com.atamanahmet.vinylexchange.repository.payment.PaymentTransactionRepository;
import com.atamanahmet.vinylexchange.service.order.OrderService;
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
class PaymentServiceTest {

    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    @Mock private PaymentPort paymentPort;
    @Mock private OrderService orderService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private IyzicoProperties paymentProperties;

    @InjectMocks
    private PaymentService paymentService;

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
                .totalPrice(10000L)
                .paymentExpiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    /**
     * Happy path, no existing transaction, creates new one and returns form
     */
    @Test
    void initiatePayment_createsNewTransaction_returnsSuccess() {
        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentProperties.getCallbackUrl()).thenReturn("https://callback.test");

        PaymentTransaction savedTx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();
        when(paymentTransactionRepository.save(any())).thenReturn(savedTx);

        PaymentInitiateResponse fakeResponse = PaymentInitiateResponse.success("TOKEN-123", "<form/>");
        when(paymentPort.initiateCheckoutForm(any(), any())).thenReturn(fakeResponse);

        PaymentInitiateResponse result = paymentService.initiatePayment(orderId, buyerId);

        assertThat(result.success()).isTrue();
        assertThat(result.providerPaymentId()).isEqualTo("TOKEN-123");
        verify(paymentTransactionRepository, times(2)).save(any());
    }

    /**
     * Buyer clicks pay twice, must reuse existing transaction, not create duplicate
     */
    @Test
    void initiatePayment_existingTransaction_reusesIt() {
        PaymentTransaction existingTx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingTx));
        when(paymentProperties.getCallbackUrl()).thenReturn("https://callback.test");

        PaymentInitiateResponse fakeResponse = PaymentInitiateResponse.success("TOKEN-456", "<form/>");
        when(paymentPort.initiateCheckoutForm(any(), any())).thenReturn(fakeResponse);
        when(paymentTransactionRepository.save(any())).thenReturn(existingTx);

        paymentService.initiatePayment(orderId, buyerId);

        verify(paymentTransactionRepository, times(1)).save(any());
    }

    /**
     * Port returns failure, transaction already saved, token must not be set
     */
    @Test
    void initiatePayment_portReturnsFailure_returnsFailureResponse() {
        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentProperties.getCallbackUrl()).thenReturn("https://callback.test");

        PaymentTransaction savedTx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();
        when(paymentTransactionRepository.save(any())).thenReturn(savedTx);
        when(paymentPort.initiateCheckoutForm(any(), any()))
                .thenReturn(PaymentInitiateResponse.failure("Iyzico error"));

        PaymentInitiateResponse result = paymentService.initiatePayment(orderId, buyerId);

        assertThat(result.success()).isFalse();

        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(paymentTransactionRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getProviderCheckoutToken()).isNull();
    }

    /**
     * Different user trying to pay for someone else's order
     */
    @Test
    void initiatePayment_wrongBuyer_throwsUnauthorized() {
        UUID otherUser = UUID.randomUUID();
        when(orderService.getOrderById(orderId)).thenReturn(order);

        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, otherUser))
                .isInstanceOf(UnauthorizedActionException.class);

        verifyNoInteractions(paymentTransactionRepository);
    }

    /**
     * Order already paid or in wrong state, must reject
     */
    @Test
    void initiatePayment_wrongOrderStatus_throwsInvalidOperation() {
        order.setStatus(OrderStatus.PAID);
        when(orderService.getOrderById(orderId)).thenReturn(order);

        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, buyerId))
                .isInstanceOf(InvalidOrderOperationException.class)
                .hasMessageContaining("not awaiting payment");

        verifyNoInteractions(paymentTransactionRepository);
    }

    /**
     * Payment window expired, must reject even if order is in correct status
     */
    @Test
    void initiatePayment_expiredWindow_throwsInvalidOperation() {
        order.setPaymentExpiresAt(LocalDateTime.now().minusMinutes(5));
        when(orderService.getOrderById(orderId)).thenReturn(order);

        assertThatThrownBy(() -> paymentService.initiatePayment(orderId, buyerId))
                .isInstanceOf(InvalidOrderOperationException.class)
                .hasMessageContaining("expired");

        verifyNoInteractions(paymentTransactionRepository);
    }

    /**
     * Iyzico POSTs callback, payment verified, order must be marked paid
     * Two transitions: PENDING_PAYMENT to CAPTURED and CAPTURED to HELD
     */
    @Test
    void handleCallback_successfulVerify_marksOrderPaid() {
        PaymentVerifyResult successResult = new PaymentVerifyResult(
                true, "SUCCESS", "IYZICO-PAY-ID", "AUTH-123", "HOST-REF", 1
        );
        when(paymentPort.verifyCallback("TOKEN-123")).thenReturn(successResult);

        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByProviderCheckoutToken("TOKEN-123"))
                .thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        boolean result = paymentService.handleCallback("TOKEN-123");

        assertThat(result).isTrue();
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.HELD);
        assertThat(tx.getCapturedAt()).isNotNull();
        verify(orderService).markPaid(orderId);
        verify(paymentStatusHistoryRepository, times(2)).save(any());
    }

    /**
     * After successful callback all provider fields must be set on transaction
     * These are needed for refund and dispute resolution with Iyzico
     */
    @Test
    void handleCallback_success_setsAllProviderFields() {
        PaymentVerifyResult successResult = new PaymentVerifyResult(
                true, "SUCCESS", "IYZICO-INTERNAL-001", "AUTH-XYZ", "HOST-REF-001", 1
        );
        when(paymentPort.verifyCallback("TOKEN-FIELDS")).thenReturn(successResult);

        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByProviderCheckoutToken("TOKEN-FIELDS"))
                .thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.handleCallback("TOKEN-FIELDS");

        assertThat(tx.getProviderInternalPaymentId()).isEqualTo("IYZICO-INTERNAL-001");
        assertThat(tx.getAuthCode()).isEqualTo("AUTH-XYZ");
        assertThat(tx.getHostReference()).isEqualTo("HOST-REF-001");
        assertThat(tx.getFraudStatus()).isEqualTo(1);
        assertThat(tx.getCapturedAt()).isNotNull();
    }

    /**
     * Iyzico verification fails, return false, touch nothing
     */
    @Test
    void handleCallback_failedVerify_returnsFalse() {
        PaymentVerifyResult failResult = new PaymentVerifyResult(
                false, "FAILURE", null, null, null, null
        );
        when(paymentPort.verifyCallback("BAD-TOKEN")).thenReturn(failResult);

        boolean result = paymentService.handleCallback("BAD-TOKEN");

        assertThat(result).isFalse();
        verifyNoInteractions(paymentTransactionRepository);
        verifyNoInteractions(orderService);
    }

    /**
     * Token verified but no matching transaction in DB, ifPresent miss, no crash
     */
    @Test
    void handleCallback_tokenNotFound_noSideEffects() {
        PaymentVerifyResult successResult = new PaymentVerifyResult(
                true, "SUCCESS", "PAY-ID", "AUTH", "HOST", 1
        );
        when(paymentPort.verifyCallback("GHOST-TOKEN")).thenReturn(successResult);
        when(paymentTransactionRepository.findByProviderCheckoutToken("GHOST-TOKEN"))
                .thenReturn(Optional.empty());

        boolean result = paymentService.handleCallback("GHOST-TOKEN");

        assertThat(result).isTrue();
        verify(paymentTransactionRepository, never()).save(any());
        verifyNoInteractions(orderService);
    }

    /**
     * Shipment recorded on transaction, shippedAt must be set
     * Payment status does not change here, funds stay HELD until delivery
     */
    @Test
    void onOrderShipped_setsShippedAt() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.HELD)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);

        paymentService.onOrderShipped(new OrderShippedEvent(orderId));

        assertThat(tx.getShippedAt()).isNotNull();
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.HELD);
        verify(paymentTransactionRepository).save(tx);
    }

    /**
     * Buyer confirms delivery, funds must be released to seller
     * Payment: HELD to RELEASED to COMPLETED, order completeOrder called
     */
    @Test
    void onOrderDelivered_heldPayment_releasesFundsToSeller() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.HELD)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));
        when(paymentPort.approvePayoutToSeller(tx)).thenReturn("PAYOUT-ID-001");
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.onOrderDelivered(new OrderDeliveredEvent(orderId));

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(tx.getProviderPayoutId()).isEqualTo("PAYOUT-ID-001");
        assertThat(tx.getPayoutApprovedAt()).isNotNull();
        verify(orderService).completeOrder(orderId);
    }

    /**
     * Delivery event arrives but payment is already COMPLETED or REFUNDED
     * Guard must skip, no double release
     */
    @Test
    void onOrderDelivered_nonHeldPayment_ignored() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.COMPLETED)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));

        paymentService.onOrderDelivered(new OrderDeliveredEvent(orderId));

        verify(paymentPort, never()).approvePayoutToSeller(any());
        verify(orderService, never()).completeOrder(any());
        verify(paymentTransactionRepository, never()).save(any());
    }

    /**
     * Order cancelled while funds are held, buyer must be refunded
     */
    @Test
    void onOrderCancelled_heldPayment_issuesRefund() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.HELD)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.onOrderCancelled(new OrderCancelledEvent(orderId));

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentPort).refundToBuyer(tx);
    }

    /**
     * Cancellation event arrives but payment is not HELD (PENDING_PAYMENT)
     * No refund should happen, nothing was captured yet
     */
    @Test
    void onOrderCancelled_nonHeldPayment_ignored() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));

        paymentService.onOrderCancelled(new OrderCancelledEvent(orderId));

        verify(paymentPort, never()).refundToBuyer(any());
        verify(paymentTransactionRepository, never()).save(any());
    }

    /**
     * Dispute reason must be recorded on transaction when dispute opens
     * Payment status stays HELD, only reason field and history entry change
     */
    @Test
    void onDisputeOpened_setsDisputeReason() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.HELD)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.onDisputeOpened(new DisputeOpenedEvent(orderId, DisputeReason.ITEM_NOT_RECEIVED));

        assertThat(tx.getDisputeReason()).isEqualTo(DisputeReason.ITEM_NOT_RECEIVED.name());
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.HELD);
    }

    /**
     * Admin resolves dispute in seller's favor, release funds
     */
    @Test
    void onDisputeResolved_forSeller_releasesFunds() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.HELD)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));
        when(paymentPort.approvePayoutToSeller(tx)).thenReturn("PAYOUT-DISPUTE-001");
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.onDisputeResolved(
                new DisputeResolvedEvent(orderId, DisputeResolvedEvent.Resolution.FOR_SELLER)
        );

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentPort).approvePayoutToSeller(tx);
        verify(orderService, never()).refundOrder(any());
    }

    /**
     * Admin resolves dispute in buyer's favor, refund buyer, refundOrder called
     */
    @Test
    void onDisputeResolved_forBuyer_issuesRefund() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.HELD)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.onDisputeResolved(
                new DisputeResolvedEvent(orderId, DisputeResolvedEvent.Resolution.FOR_BUYER)
        );

        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentPort).refundToBuyer(tx);
        verify(orderService).refundOrder(orderId);
    }

    /**
     * Every status change must write a history record
     * Verifies history is saved and event is published on handleCallback
     */
    @Test
    void handleCallback_success_writesHistoryAndPublishesEvents() {
        PaymentVerifyResult successResult = new PaymentVerifyResult(
                true, "SUCCESS", "IYZICO-PAY-ID", "AUTH", "HOST", 1
        );
        when(paymentPort.verifyCallback("TOKEN-999")).thenReturn(successResult);

        PaymentTransaction tx = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(sellerId)
                .status(PaymentStatus.PENDING_PAYMENT)
                .amountKurus(10000L)
                .build();

        when(paymentTransactionRepository.findByProviderCheckoutToken("TOKEN-999"))
                .thenReturn(Optional.of(tx));
        when(paymentTransactionRepository.save(any())).thenReturn(tx);
        when(paymentStatusHistoryRepository.save(any())).thenReturn(null);

        paymentService.handleCallback("TOKEN-999");

        verify(paymentStatusHistoryRepository, times(2)).save(any());
        verify(eventPublisher, times(2)).publishEvent(any(PaymentStateChangedEvent.class));
    }
}