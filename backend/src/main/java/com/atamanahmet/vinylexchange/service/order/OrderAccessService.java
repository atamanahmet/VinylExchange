package com.atamanahmet.vinylexchange.service.order;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderAccessService {

    private final OrderRepository orderRepository;

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.AWAITING_PAYMENT,
            OrderStatus.PAID,
            OrderStatus.AWAITING_SHIPMENT
    );

    /**
     * Buyer or seller can view their own order
     */
    public void assertCanView(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new UnauthorizedActionException("Order not found"));
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new UnauthorizedActionException("Access denied for this order");
        }
    }

    public void validateBuyer(Order order, UUID userId) {
        if (!order.getBuyerId().equals(userId)) {
            throw new UnauthorizedActionException("Not the buyer of this order");
        }
    }

    public void validateSeller(Order order, UUID userId) {
        if (!order.getSellerId().equals(userId)) {
            throw new UnauthorizedActionException("Not the seller of this order");
        }
    }

    public boolean isBuyer(Order order, UUID userId) {
        return order.getBuyerId().equals(userId);
    }

    /**
     * Returns true if any active order exists for this listing
     * Used by ListingService to block price updates
     */
    public boolean hasActiveOrderForListing(UUID listingId) {
        return orderRepository.existsByOrderItems_Listing_IdAndStatusIn(
                listingId, ACTIVE_ORDER_STATUSES);
    }
}