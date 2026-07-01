package com.atamanahmet.vinylexchange.repository.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = "orderItems")
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @EntityGraph(attributePaths = "orderItems")
    List<Order> findAllByBuyerId(UUID buyerId);

    @Query(value = "SELECT nextVal('order_number_seq')", nativeQuery = true)
    Long getNextOrderNumber();

    @EntityGraph(attributePaths = "orderItems")
    List<Order> findAllBySellerId(UUID sellerId);

    List<Order> findAllByStatusAndPaymentExpiresAtBefore(OrderStatus status, LocalDateTime dateTime);

    boolean existsByOrderItems_ListingIdAndStatusIn(UUID listingId, Set<OrderStatus> statuses);

    @Query("SELECT o.id FROM Order o WHERE o.status IN :statuses AND o.updatedAt < :cutoff AND (o.shippingAddressSnapshot IS NOT NULL OR o.billingAddressSnapshot IS NOT NULL)")
    List<UUID> findIdsForKvkkPurge(@Param("statuses") List<OrderStatus> statuses, @Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Order o SET o.shippingAddressSnapshot = NULL, o.billingAddressSnapshot = NULL WHERE o.id IN :ids")
    void nullifyAddressSnapshots(@Param("ids") List<UUID> ids);

    Optional<Order> findByShipmentOrderId(String shipmentOrderId);
}
