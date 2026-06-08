package com.atamanahmet.vinylexchange.service.payment;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.PaymentStatusHistory;
import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.dto.payment.PaymentVerifyResult;
import com.atamanahmet.vinylexchange.event.DisputeOpenedEvent;
import com.atamanahmet.vinylexchange.event.DisputeResolvedEvent;
import com.atamanahmet.vinylexchange.event.PaymentStateChangedEvent;
import com.atamanahmet.vinylexchange.event.OrderCancelledEvent;
import com.atamanahmet.vinylexchange.event.OrderDeliveredEvent;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.infrastructure.payment.IyzicoProperties;
import com.atamanahmet.vinylexchange.infrastructure.payment.PaymentPort;
import com.atamanahmet.vinylexchange.repository.payment.PaymentStatusHistoryRepository;
import com.atamanahmet.vinylexchange.repository.payment.PaymentTransactionRepository;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {


    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final PaymentPort paymentPort;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;
    private final IyzicoProperties paymentProperties;

    /**
     * Called by PaymentController when user clicks pay
     * Creates PaymentTransaction and gets Iyzico checkout form
     * If transaction already exists, re-uses it
     * Handles user clicking pay twice without creating duplicate
     */
    @Transactional
    public PaymentInitiateResponse initiatePayment(UUID orderId, UUID buyerId) {
        Order order = orderService.getOrderById(orderId);

        if (!order.getBuyerId().equals(buyerId)) {
            throw new UnauthorizedActionException("Order does not belong to this user");
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new InvalidOrderOperationException("Order is not awaiting payment: " + orderId);
        }

        if (order.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOrderOperationException("Payment window has expired for order: " + orderId);
        }

        PaymentTransaction payment = paymentTransactionRepository.findByOrderId(orderId)
                .orElseGet(() -> paymentTransactionRepository.save(
                        PaymentTransaction.builder()
                                .order(order)
                                .sellerId(order.getSellerId())
                                .status(PaymentStatus.PENDING_PAYMENT)
                                .amountKurus(order.getTotalPrice())
                                .build()
                ));

        PaymentInitiateResponse response = paymentPort.initiateCheckoutForm(
                payment, paymentProperties.getCallbackUrl());

        if (response.success()) {
            payment.setProviderPaymentId(response.providerPaymentId());
            paymentTransactionRepository.save(payment);
        }

        return response;
    }

    /**
     * Called by PaymentController when Iyzico POSTs to our callback URL
     * Verifies token with Iyzico, marks order paid or cancels it
     */
    @Transactional
    public boolean handleCallback(String token) {
        PaymentVerifyResult result = paymentPort.verifyCallback(token);

        if (!result.success()) {
            log.warn("Payment failed or unverified token={} status={}", token, result.paymentStatus());
            return false;
        }

        paymentTransactionRepository.findByProviderPaymentId(token).ifPresent(payment -> {

            payment.setProviderInternalPaymentId(result.providerInternalPaymentId());
            payment.setAuthCode(result.authCode());
            payment.setHostReference(result.hostReference());
            payment.setFraudStatus(result.fraudStatus());
            payment.setCapturedAt(LocalDateTime.now());

            transition(payment, PaymentStatus.CAPTURED, "Payment captured via callback", "SYSTEM");
            transition(payment, PaymentStatus.HELD, "Funds held awaiting delivery", "SYSTEM");
            paymentTransactionRepository.save(payment);
            orderService.markPaid(payment.getOrder().getId());
            log.info("Payment confirmed for order={}", payment.getOrder().getId());
        });

        return true;
    }

    /**
     * Listens for shipment, records audit timestamp on payment
     * Does not change payment status, funds stay HELD until delivery
     */
    @EventListener
    @Transactional
    public void onOrderShipped(OrderShippedEvent event) {
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            payment.setShippedAt(LocalDateTime.now());
            paymentTransactionRepository.save(payment);
            log.info("Shipment recorded on payment for order={}", event.orderId());
        });
    }

    /**
     * Listens for delivery confirmation, releases funds to seller immediately
     * Fires for both manual buyer confirmation and auto-confirm
     */
    @EventListener
    @Transactional
    public void onOrderDelivered(OrderDeliveredEvent event) {
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            if (payment.getStatus() != PaymentStatus.HELD) {
                log.warn("Delivery event for order={} ignored, payment status={}", event.orderId(), payment.getStatus());
                return;
            }
            releaseFundsToSeller(payment, "Delivery confirmed");
            log.info("Funds released for order={}", event.orderId());
        });
    }

    /**
     * Listens for cancellation, refunds buyer if funds are held
     */
    @EventListener
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            if (payment.getStatus() != PaymentStatus.HELD) {
                log.warn("Cancellation event for order={} ignored, payment status={}", event.orderId(), payment.getStatus());
                return;
            }
            issueRefund(payment, "Order cancelled by buyer");
            log.info("Refund issued for cancelled order={}", event.orderId());
        });
    }

    /**
     * Listens for dispute, holds funds until admin resolves
     * Funds are already HELD, this is just an audit transition
     */
    @EventListener
    @Transactional
    public void onDisputeOpened(DisputeOpenedEvent event) {
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            if (payment.getStatus() != PaymentStatus.HELD) {
                log.warn("Dispute event for order={} ignored, payment status={}", event.orderId(), payment.getStatus());
                return;
            }
            payment.setDisputeReason(event.reason().name());
            transition(payment, PaymentStatus.HELD, "Dispute opened: " + event.reason(), "BUYER");
            paymentTransactionRepository.save(payment);
            log.info("Dispute recorded on payment for order={}", event.orderId());
        });
    }

    /**
     * Listens for dispute resolution, releases or refunds based on admin decision
     */
    @EventListener
    @Transactional
    public void onDisputeResolved(DisputeResolvedEvent event) {
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            if (payment.getStatus() != PaymentStatus.HELD) {
                log.warn("Dispute resolution for order={} ignored, payment status={}", event.orderId(), payment.getStatus());
                return;
            }

            if (event.resolution() == DisputeResolvedEvent.Resolution.FOR_SELLER) {
                releaseFundsToSeller(payment, "Dispute resolved in seller's favor");
            } else {
                issueRefund(payment, "Dispute resolved in buyer's favor");
                orderService.refundOrder(event.orderId());
            }

            log.info("Dispute resolved for order={} resolution={}", event.orderId(), event.resolution());
        });
    }

    //
    // PRIVATE HELPERS
    //

    private void releaseFundsToSeller(PaymentTransaction payment, String note) {
        String payoutId = paymentPort.approvePayoutToSeller(payment);
        payment.setProviderPayoutId(payoutId);
        payment.setPayoutApprovedAt(LocalDateTime.now());
        transition(payment, PaymentStatus.RELEASED, note, "SYSTEM");
        transition(payment, PaymentStatus.COMPLETED, "Payout complete", "SYSTEM");
        paymentTransactionRepository.save(payment);
        orderService.completeOrder(payment.getOrder().getId());
    }

    private void issueRefund(PaymentTransaction payment, String note) {
        paymentPort.refundToBuyer(payment);
        transition(payment, PaymentStatus.REFUNDED, note, "SYSTEM");
        paymentTransactionRepository.save(payment);
    }

    /**
     * Single place where payment status changes, records history and publishes event
     * triggeredBy is actor type: SYSTEM, BUYER, SELLER
     */
    private void transition(PaymentTransaction payment, PaymentStatus newStatus, String note, String triggeredBy) {
        PaymentStatus previous = payment.getStatus();
        payment.setStatus(newStatus);

        paymentStatusHistoryRepository.save(PaymentStatusHistory.builder()
                .paymentTransaction(payment)
                .fromStatus(previous)
                .toStatus(newStatus)
                .triggeredBy(triggeredBy)
                .occurredAt(LocalDateTime.now())
                .note(note)
                .build());

        eventPublisher.publishEvent(new PaymentStateChangedEvent(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getOrder().getBuyerId(),
                payment.getSellerId(),
                previous,
                newStatus
        ));
    }

    public Optional<PaymentTransaction> findByOrderId(UUID orderId) {
        return paymentTransactionRepository.findByOrderId(orderId);
    }
}