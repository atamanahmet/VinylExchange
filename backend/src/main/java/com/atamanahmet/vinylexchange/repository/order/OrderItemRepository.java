package com.atamanahmet.vinylexchange.repository.order;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

}
