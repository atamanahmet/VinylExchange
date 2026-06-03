package com.atamanahmet.vinylexchange.repository.order;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByBuyerId(UUID buyerId);

    @Query(value = "SELECT nextVal('order_number_seq')", nativeQuery = true)
    Long getNextOrderNumber();

}
