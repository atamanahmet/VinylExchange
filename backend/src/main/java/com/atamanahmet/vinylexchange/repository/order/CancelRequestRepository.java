package com.atamanahmet.vinylexchange.repository.order;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.CancelRequest;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.enums.CancelRequestStatus;

@Repository
public interface CancelRequestRepository extends JpaRepository<CancelRequest, UUID> {

    boolean existsByOrderAndStatus(Order order, CancelRequestStatus status);

    /** Find all pending requests where the seller is the reviewer */
    @Query("SELECT cr FROM CancelRequest cr " +
            "WHERE cr.order.sellerId = :sellerId " +
            "AND cr.status = 'PENDING'")
    List<CancelRequest> findPendingRequestsForSeller(@Param("sellerId") UUID sellerId);

    List<CancelRequest> findByOrderId(UUID orderId);
}