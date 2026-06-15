package com.atamanahmet.vinylexchange.repository.order;

import com.atamanahmet.vinylexchange.domain.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
    List<OrderStatusHistory> findAllByOrderIdOrderByOccurredAtAsc(UUID orderId);
}