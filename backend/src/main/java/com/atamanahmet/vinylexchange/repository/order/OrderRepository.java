package com.atamanahmet.vinylexchange.repository.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByBuyerId(UUID buyerId);

    @Query(value = "SELECT nextVal('order_number_seq')", nativeQuery = true)
    Long getNextOrderNumber();

    List<Order> findAllBySellerId(UUID sellerId);

    List<Order> findAllByStatusAndPaymentExpiresAtBefore(OrderStatus status, LocalDateTime dateTime);
}
