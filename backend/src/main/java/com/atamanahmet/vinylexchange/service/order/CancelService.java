package com.atamanahmet.vinylexchange.service.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.CancelRequest;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderItem;
import com.atamanahmet.vinylexchange.event.OrderCancelledEvent;
import com.atamanahmet.vinylexchange.repository.order.CancelRequestRepository;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.enums.CancelRequestStatus;
import com.atamanahmet.vinylexchange.domain.enums.OrderItemStatus;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;

import com.atamanahmet.vinylexchange.exception.CancelRequestNotFoundException;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.ListingNotFoundException;
import com.atamanahmet.vinylexchange.exception.OrderItemNotFoundException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;

import jakarta.transaction.Transactional;

@Service
public class CancelService {
    private static final Logger logger = LoggerFactory.getLogger(CancelService.class);
    private static final int AUTO_APPROVE_HOURS = 2;

    private final CancelRequestRepository cancelRequestRepository;
    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final ListingService listingService;
    private final ApplicationEventPublisher eventPublisher;   // added

    public CancelService(
            CancelRequestRepository cancelRequestRepository,
            OrderItemService orderItemService,
            OrderService orderService,
            ListingService listingService,
            ApplicationEventPublisher eventPublisher) {        // added
        this.cancelRequestRepository = cancelRequestRepository;
        this.orderItemService = orderItemService;
        this.orderService = orderService;
        this.listingService = listingService;
        this.eventPublisher = eventPublisher;                  // added
    }

    @Transactional
    public CancelRequest requestCancel(UUID orderItemId, UUID userId, String reason) {

        Order order = orderService.getOrderById(orderItemId)
                .orElseThrow(() -> new InvalidOrderOperationException("Order not found"));

        if (!orderItem.getOrder().getBuyerId().equals(userId)) {
            throw new UnauthorizedActionException("User is not own this item");
        }

        if (orderItem.getStatus() == OrderItemStatus.DELIVERED ||
                orderItem.getStatus() == OrderItemStatus.CANCELLED ||
                orderItem.getStatus() == OrderItemStatus.CANCEL_PENDING) {

            throw new InvalidOrderOperationException(
                    "Cannot request cancel for item with status: " + orderItem.getStatus());
        }

        boolean hasPendingRequest = cancelRequestRepository
                .existsByOrderItemAndStatus(orderItem, CancelRequestStatus.PENDING);

        if (hasPendingRequest) {
            throw new InvalidOrderOperationException("Cancel request already pending for this item");
        }

        CancelRequest request = CancelRequest.builder()
                .orderItem(orderItem)
                .requestedBy(userId)
                .status(CancelRequestStatus.PENDING)
                .previousOrderStatus(orderItem.getStatus())
                .reason(reason)
                .requestedAt(LocalDateTime.now())
                .build();

        if (shouldAutoApprove(orderItem)) {

            if (orderItem.getStatus() != OrderItemStatus.PENDING &&
                    orderItem.getStatus() != OrderItemStatus.PROCESSING) {

                orderItem.setStatus(OrderItemStatus.CANCEL_PENDING);
                logger.warn("Cannot approve, item status changed {}", orderItem.getStatus());
            } else {

                request.setStatus(CancelRequestStatus.AUTO_APPROVED);
                request.setReviewedAt(LocalDateTime.now());

                orderItem.setStatus(OrderItemStatus.CANCELLED);
                processCancel(orderItem);
            }
        } else {
            orderItem.setStatus(OrderItemStatus.CANCEL_PENDING);
        }

        orderItemService.saveOrderItem(orderItem);

        return cancelRequestRepository.save(request);
    }

    @Transactional
    public CancelRequest approveCancel(UUID requestId, UUID reviewerId, String reviewNote) {

        CancelRequest request = cancelRequestRepository.findById(requestId)
                .orElseThrow(() -> new CancelRequestNotFoundException("Cancel request not found"));

        if (request.getStatus() != CancelRequestStatus.PENDING) {
            throw new InvalidOrderOperationException("Can only approve pending cancel requests");
        }

        OrderItem orderItem = request.getOrderItem();

        if (!orderItem.getSellerId().equals(reviewerId)) {
            throw new UnauthorizedActionException("Only seller or admin can review cancel requests");
        }

        request.setStatus(CancelRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(reviewerId);
        request.setReviewNote(reviewNote);

        orderItem.setStatus(OrderItemStatus.CANCELLED);
        orderItemService.saveOrderItem(orderItem);

        processCancel(orderItem);

        return cancelRequestRepository.save(request);
    }

    @Transactional
    public CancelRequest rejectCancel(UUID requestId, UUID reviewerId, String reviewNote) {

        CancelRequest request = cancelRequestRepository.findById(requestId)
                .orElseThrow(() -> new CancelRequestNotFoundException("Request not found"));

        if (request.getStatus() != CancelRequestStatus.PENDING) {
            throw new InvalidOrderOperationException("Can only reject pending cancel requests");
        }

        OrderItem orderItem = request.getOrderItem();

        request.setStatus(CancelRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(reviewerId);
        request.setReviewNote(reviewNote);

        orderItem.setStatus(request.getPreviousOrderStatus());
        orderItemService.saveOrderItem(orderItem);

        return cancelRequestRepository.save(request);
    }

    private boolean shouldAutoApprove(OrderItem orderItem) {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(AUTO_APPROVE_HOURS);
        return orderItem.getCreatedAt().isAfter(timeLimit) &&
                orderItem.getStatus() == OrderItemStatus.PENDING;
    }

    private void processCancel(OrderItem orderItem) {

        Order order = orderItem.getOrder();

        Long newTotalPrice = order.getOrderItems().stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELLED)
                .mapToLong(OrderItem::getSubTotal)
                .sum();

        order.setTotalPrice(newTotalPrice);

        try {
            listingService.restoreStock(orderItem.getListingId(), orderItem.getQuantity());
        } catch (ListingNotFoundException e) {
            logger.warn("Could not restore stock for listing {}: {}",
                    orderItem.getListingId(), e.getMessage());
        }

        boolean allItemsCancelled = order.getOrderItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.CANCELLED);

        if (allItemsCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
            logger.info("All items cancelled, order {} marked as cancelled", order.getId());
        }

        orderService.saveOrder(order);

        // notify escrow to refund if all items are cancelled
        if (allItemsCancelled) {
            eventPublisher.publishEvent(new OrderCancelledEvent(order.getId()));
        }
    }

    @Transactional
    public void cancelCancellationRequest(UUID requestId, UUID userId) {

        CancelRequest request = cancelRequestRepository.findById(requestId)
                .orElseThrow(() -> new CancelRequestNotFoundException("Cancel request not found"));

        if (!request.getRequestedBy().equals(userId)) {
            throw new UnauthorizedActionException("User does not own this cancellation request");
        }

        if (request.getStatus() != CancelRequestStatus.PENDING) {
            throw new InvalidOrderOperationException("Can only cancel pending requests");
        }

        OrderItem orderItem = request.getOrderItem();

        request.setStatus(CancelRequestStatus.CANCELLED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(userId);

        cancelRequestRepository.save(request);

        orderItem.setStatus(request.getPreviousOrderStatus());
        orderItemService.saveOrderItem(orderItem);
    }

    public List<CancelRequest> getPendingRequestsForSeller(UUID sellerId) {
        return cancelRequestRepository.findPendingRequestsForSeller(sellerId);
    }

    public List<CancelRequest> getRequestsForOrderItem(UUID orderItemId) {
        return cancelRequestRepository.findByOrderItemId(orderItemId);
    }
}