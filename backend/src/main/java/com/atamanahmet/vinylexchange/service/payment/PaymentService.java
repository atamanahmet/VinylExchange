package com.atamanahmet.vinylexchange.service.payment;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.PaymentStatusHistory;
import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;
import com.atamanahmet.vinylexchange.dto.payment.PaymentCallbackOutcome;
import com.atamanahmet.vinylexchange.dto.payment.PaymentHistoryDto;
import com.atamanahmet.vinylexchange.dto.payment.PaymentHistoryEventDto;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.dto.payment.PaymentVerifyResult;
import com.atamanahmet.vinylexchange.dto.payment.RefundReviewPaymentDto;
import com.atamanahmet.vinylexchange.event.DisputeOpenedEvent;
import com.atamanahmet.vinylexchange.event.DisputeResolvedEvent;
import com.atamanahmet.vinylexchange.event.OrderCancelledEvent;
import com.atamanahmet.vinylexchange.event.OrderDeliveredEvent;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.event.PaymentStateChangedEvent;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.InvalidPaymentStatusTransitionException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.infrastructure.payment.IyzicoProperties;
import com.atamanahmet.vinylexchange.infrastructure.payment.PaymentPort;
import com.atamanahmet.vinylexchange.mapper.PaymentMapper;
import com.atamanahmet.vinylexchange.repository.payment.PaymentStatusHistoryRepository;
import com.atamanahmet.vinylexchange.repository.payment.PaymentTransactionRepository;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import com.atamanahmet.vinylexchange.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final PaymentPort paymentPort;
    private final OrderService orderService;
    private final UserService userService;
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
        Order order = orderService.requireOrderWithItems(orderId);

        if (!order.getBuyerId().equals(buyerId)) {
            throw new UnauthorizedActionException("Order does not belong to this user");
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new InvalidOrderOperationException("Order is not awaiting payment: " + orderId);
        }

        if (order.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOrderOperationException("Payment window has expired for order: " + orderId);
        }

        PaymentTransaction payment = paymentTransactionRepository.findByOrderIdWithOrderAndItems(orderId)
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
            payment.setProviderCheckoutToken(response.providerPaymentId());
            paymentTransactionRepository.save(payment);
        }

        return response;
    }

    /**
     * Called by PaymentController when Iyzico POSTs to our callback URL
     * Verifies token with Iyzico, marks order paid or flags refund review
     */
    @Transactional
    public PaymentCallbackOutcome handleCallback(String token) {
        PaymentVerifyResult result = paymentPort.verifyCallback(token);

        if (!result.success()) {
            log.warn("Payment failed or unverified token={} status={}", token, result.paymentStatus());
            return PaymentCallbackOutcome.VERIFICATION_FAILED;
        }

        Optional<PaymentTransaction> paymentOptional = paymentTransactionRepository
                .findByProviderCheckoutToken(token);

        if (paymentOptional.isEmpty()) {
            log.warn("Verified payment token has no matching transaction token={}", token);
            return PaymentCallbackOutcome.PROCESSED;
        }

        PaymentTransaction payment = paymentOptional.get();
        PaymentStatus status = payment.getStatus();

        if (status == PaymentStatus.HELD) {
            log.info("Duplicate payment callback ignored orderId={} paymentId={} status={}",
                    payment.getOrder().getId(), payment.getId(), status);
            return PaymentCallbackOutcome.ALREADY_HELD;
        }

        if (status == PaymentStatus.PENDING_PAYMENT) {
            applyVerifiedProviderFields(payment, result);
            transition(payment, PaymentStatus.HELD, "Payment captured and held via callback", "SYSTEM");
            paymentTransactionRepository.save(payment);
            orderService.markPaid(payment.getOrder().getId());
            log.info("Payment confirmed for order={}", payment.getOrder().getId());
            return PaymentCallbackOutcome.PROCESSED;
        }

        if (status.isTerminal() || status == PaymentStatus.RELEASED) {
            applyVerifiedProviderFields(payment, result);
            payment.setRefundReviewRequired(true);
            paymentTransactionRepository.save(payment);
            log.warn(
                    "Late verified callback on terminal payment orderId={} paymentId={} status={} token={}",
                    payment.getOrder().getId(), payment.getId(), status, token);
            return PaymentCallbackOutcome.REFUND_REVIEW_REQUIRED;
        }

        log.warn("Unexpected payment status on verified callback orderId={} paymentId={} status={} token={}",
                payment.getOrder().getId(), payment.getId(), status, token);
        applyVerifiedProviderFields(payment, result);
        payment.setRefundReviewRequired(true);
        paymentTransactionRepository.save(payment);
        return PaymentCallbackOutcome.REFUND_REVIEW_REQUIRED;
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
        PaymentTransaction payment = requirePaymentForOrder(event.orderId(), "delivery");
        releaseFundsToSeller(payment, "Delivery confirmed");
        log.info("Funds released for order={}", event.orderId());
    }

    /**
     * Listens for cancellation, cancels uncaptured payments or refunds held funds
     */
    @EventListener
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        paymentTransactionRepository.findByOrderId(event.orderId()).ifPresent(payment -> {
            switch (payment.getStatus()) {
                case PENDING_PAYMENT -> {
                    transition(payment, PaymentStatus.CANCELLED, "Order cancelled before capture", "SYSTEM");
                    paymentTransactionRepository.save(payment);
                    log.info("Uncaptured payment cancelled for order={}", event.orderId());
                }
                case HELD -> {
                    issueRefund(payment, "Order cancelled by buyer");
                    log.info("Refund issued for cancelled order={}", event.orderId());
                }
                default -> throw new InvalidPaymentStatusTransitionException(
                        "Cannot cancel payment from status " + payment.getStatus()
                                + " for order " + event.orderId());
            }
        });
    }

    /**
     * Listens for dispute, records reason and audit trail while funds stay HELD
     */
    @EventListener
    @Transactional
    public void onDisputeOpened(DisputeOpenedEvent event) {
        PaymentTransaction payment = requirePaymentForOrder(event.orderId(), "dispute opened");

        if (payment.getStatus() != PaymentStatus.HELD) {
            throw new InvalidPaymentStatusTransitionException(
                    "Cannot record dispute, payment is " + payment.getStatus()
                            + " for order " + event.orderId());
        }

        payment.setDisputeReason(event.reason().name());
        recordPaymentAudit(payment, "Dispute opened: " + event.reason(), "BUYER");
        paymentTransactionRepository.save(payment);
        log.info("Dispute recorded on payment for order={}", event.orderId());
    }

    /**
     * Listens for dispute resolution, releases or refunds based on admin decision
     */
    @EventListener
    @Transactional
    public void onDisputeResolved(DisputeResolvedEvent event) {
        PaymentTransaction payment = requirePaymentForOrder(event.orderId(), "dispute resolution");

        if (payment.getStatus() != PaymentStatus.HELD) {
            throw new InvalidPaymentStatusTransitionException(
                    "Cannot resolve dispute, payment is " + payment.getStatus()
                            + " for order " + event.orderId());
        }

        if (event.resolution() == DisputeResolvedEvent.Resolution.FOR_SELLER) {
            releaseFundsToSeller(payment, "Dispute resolved in seller's favor");
        } else {
            issueRefund(payment, "Dispute resolved in buyer's favor");
            orderService.refundOrder(event.orderId());
        }

        log.info("Dispute resolved for order={} resolution={}", event.orderId(), event.resolution());
    }

    private void releaseFundsToSeller(PaymentTransaction payment, String note) {
        if (!payment.getStatus().canTransitionTo(PaymentStatus.RELEASED)) {
            throw new InvalidPaymentStatusTransitionException(payment.getStatus(), PaymentStatus.RELEASED);
        }

        String payoutId = paymentPort.approvePayoutToSeller(payment);
        payment.setProviderPayoutId(payoutId);
        payment.setPayoutApprovedAt(LocalDateTime.now());
        transition(payment, PaymentStatus.RELEASED, note, "SYSTEM");
        transition(payment, PaymentStatus.COMPLETED, "Payout complete", "SYSTEM");
        paymentTransactionRepository.save(payment);
        orderService.completeOrder(payment.getOrder().getId());
    }

    private void issueRefund(PaymentTransaction payment, String note) {
        if (!payment.getStatus().canTransitionTo(PaymentStatus.REFUNDED)) {
            throw new InvalidPaymentStatusTransitionException(payment.getStatus(), PaymentStatus.REFUNDED);
        }

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

        if (!previous.canTransitionTo(newStatus)) {
            throw new InvalidPaymentStatusTransitionException(previous, newStatus);
        }

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

    private void recordPaymentAudit(PaymentTransaction payment, String note, String triggeredBy) {
        PaymentStatus status = payment.getStatus();
        paymentStatusHistoryRepository.save(PaymentStatusHistory.builder()
                .paymentTransaction(payment)
                .fromStatus(status)
                .toStatus(status)
                .triggeredBy(triggeredBy)
                .occurredAt(LocalDateTime.now())
                .note(note)
                .build());
    }

    private PaymentTransaction requirePaymentForOrder(UUID orderId, String action) {
        return paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new InvalidPaymentStatusTransitionException(
                        "Missing payment transaction for order " + orderId + " during " + action));
    }

    private void applyVerifiedProviderFields(PaymentTransaction payment, PaymentVerifyResult result) {
        payment.setProviderInternalPaymentId(result.providerInternalPaymentId());
        payment.setAuthCode(result.authCode());
        payment.setHostReference(result.hostReference());
        payment.setFraudStatus(result.fraudStatus());
        payment.setCapturedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<RefundReviewPaymentDto> listRefundReviewRequired() {
        return paymentTransactionRepository.findAllByRefundReviewRequiredTrueOrderByCapturedAtDesc()
                .stream()
                .map(RefundReviewPaymentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryDto> listPaymentHistoryForBuyer(UUID buyerId) {
        List<PaymentTransaction> payments =
                paymentTransactionRepository.findAllByOrderBuyerIdOrderByCreatedAtDesc(buyerId);

        if (payments.isEmpty()) {
            return List.of();
        }

        List<UUID> paymentIds = payments.stream().map(PaymentTransaction::getId).toList();
        Map<UUID, List<PaymentHistoryEventDto>> eventsByPaymentId =
                paymentStatusHistoryRepository
                        .findAllByPaymentTransactionIdInOrderByOccurredAtAsc(paymentIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                history -> history.getPaymentTransaction().getId(),
                                Collectors.mapping(PaymentMapper::toEventDto, Collectors.toList())));

        Map<UUID, String> sellerUsernames = userService.findUsernamesByIds(
                payments.stream().map(PaymentTransaction::getSellerId).collect(Collectors.toSet()));

        return payments.stream()
                .map(payment -> PaymentMapper.toHistoryDto(
                        payment,
                        sellerUsernames.get(payment.getSellerId()),
                        eventsByPaymentId.getOrDefault(payment.getId(), List.of())))
                .toList();
    }

    public Optional<PaymentTransaction> findByOrderId(UUID orderId) {
        return paymentTransactionRepository.findByOrderId(orderId);
    }
}
