package com.atamanahmet.vinylexchange.service.order;

import com.atamanahmet.vinylexchange.domain.entity.CancelRequest;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.enums.CancelRequestStatus;
import com.atamanahmet.vinylexchange.domain.enums.DisputeReason;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.event.DisputeResolvedEvent;
import com.atamanahmet.vinylexchange.exception.CancelRequestNotFoundException;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.repository.order.CancelRequestRepository;
import com.atamanahmet.vinylexchange.service.listing.ListingService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelService {

    private static final Logger logger = LoggerFactory.getLogger(CancelService.class);

    private final CancelRequestRepository cancelRequestRepository;
    private final OrderService orderService;
    private final ListingService listingService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Buyer cancels before shipment, instant by Turkish law
     * Only valid from PAID status
     */
    @Transactional
    public void cancelOrder(UUID orderId, UUID buyerId, String reason) {

        Order order = orderService.getOrderById(orderId);

        if (cancelRequestRepository.existsByOrderAndStatus(order, CancelRequestStatus.PENDING)) {
            throw new InvalidOrderOperationException("A cancel request already exists for this order");
        }

        orderService.cancelOrder(orderId, buyerId);

        restoreStock(order);

        cancelRequestRepository.save(CancelRequest.builder()
                .order(order)
                .requestedBy(buyerId)
                .status(CancelRequestStatus.APPROVED)
                .reason(reason)
                .requestedAt(LocalDateTime.now())
                .reviewedAt(LocalDateTime.now())
                .build());

        logger.info("Order cancelled orderId={} buyerId={}", orderId, buyerId);
    }

    /**
     * Buyer opens dispute after delivery within 14-day window
     */
    @Transactional
    public void openDispute(UUID orderId, UUID buyerId, DisputeReason disputeReason, String reason) {

        Order order = orderService.getOrderById(orderId);

        if (cancelRequestRepository.existsByOrderAndStatus(order, CancelRequestStatus.PENDING)) {
            throw new InvalidOrderOperationException("A dispute is already pending for this order");
        }

        orderService.openDispute(orderId, buyerId, disputeReason);

        cancelRequestRepository.save(CancelRequest.builder()
                .order(order)
                .requestedBy(buyerId)
                .status(CancelRequestStatus.PENDING)
                .disputeReason(disputeReason)
                .reason(reason)
                .disputeWindowDeadline(order.getDeliveredAt().plusDays(14))
                .requestedAt(LocalDateTime.now())
                .build());

        logger.info("Dispute opened orderId={} reason={}", orderId, disputeReason);
    }

    /**
     * Admin resolves dispute, for seller or buyer
     */
    @Transactional
    public void resolveDispute(UUID orderId, UUID adminId,
                               DisputeResolvedEvent.Resolution resolution, String reviewNote) {

        Order order = orderService.getOrderById(orderId);

        if (order.getStatus() != OrderStatus.DISPUTED) {
            throw new InvalidOrderOperationException("Order is not in disputed state");
        }

        CancelRequest request = cancelRequestRepository
                .findByOrderId(orderId)
                .stream()
                .filter(r -> r.getStatus() == CancelRequestStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new CancelRequestNotFoundException("No pending dispute for this order"));

        request.setStatus(CancelRequestStatus.APPROVED);
        request.setReviewedBy(adminId);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNote(reviewNote);

        cancelRequestRepository.save(request);

        eventPublisher.publishEvent(new DisputeResolvedEvent(orderId, resolution));

        logger.info("Dispute resolved orderId={} resolution={}", orderId, resolution);
    }

    /**
     * Restores listing stock when order is canceled
     */
    private void restoreStock(Order order) {
        order.getOrderItems().forEach(item -> {
            try {
                listingService.restoreStock(item.getListingId(), item.getQuantity());
            } catch (Exception e) {
                logger.warn("Stock restore failed listingId={}: {}", item.getListingId(), e.getMessage());
            }
        });
    }
}