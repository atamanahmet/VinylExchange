package com.atamanahmet.vinylexchange.service.order;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderItem;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.repository.order.OrderRepository;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderSchedulerService.class);

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ListingService listingService;

    /**
     * Runs every minute, finds expired unpaid orders, cancels and restores stock
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredOrders() {
        List<Order> expired = orderRepository
                .findAllByStatusAndPaymentExpiresAtBefore(
                        OrderStatus.AWAITING_PAYMENT,
                        LocalDateTime.now());

        for (Order order : expired) {
            try {
                restoreStock(order);
                orderService.cancelExpiredOrder(order.getId());
                logger.info("Expired order cancelled orderId={}", order.getId());
            } catch (Exception e) {
                logger.error("Failed to cancel expired order orderId={}", order.getId(), e);
            }
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            listingService.restoreStock(item.getListingId(), item.getQuantity());
        }
    }
}