package com.atamanahmet.vinylexchange.repository.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"order", "order.orderItems"})
    @Query("SELECT p FROM PaymentTransaction p WHERE p.order.id = :orderId")
    Optional<PaymentTransaction> findByOrderIdWithOrderAndItems(@Param("orderId") UUID orderId);

    @EntityGraph(attributePaths = "order")
    @Query("SELECT p FROM PaymentTransaction p WHERE p.order.buyerId = :buyerId ORDER BY p.createdAt DESC")
    List<PaymentTransaction> findAllByOrderBuyerIdOrderByCreatedAtDesc(@Param("buyerId") UUID buyerId);

    /**
     * Scheduler uses this to find SHIPPED transactions past their auto-confirm deadline
     */
    List<PaymentTransaction> findAllByStatusAndAutoConfirmDeadlineBefore(
            PaymentStatus status,
            LocalDateTime deadline
    );

    Optional<PaymentTransaction> findByProviderCheckoutToken(String providerPaymentId);

    @EntityGraph(attributePaths = "order")
    List<PaymentTransaction> findAllByRefundReviewRequiredTrueOrderByCapturedAtDesc();
}