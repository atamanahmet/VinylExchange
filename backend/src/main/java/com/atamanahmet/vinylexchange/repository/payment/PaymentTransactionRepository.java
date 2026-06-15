package com.atamanahmet.vinylexchange.repository.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByOrderId(UUID orderId);

    /**
     * Scheduler uses this to find SHIPPED transactions past their auto-confirm deadline
     */
    List<PaymentTransaction> findAllByStatusAndAutoConfirmDeadlineBefore(
            PaymentStatus status,
            LocalDateTime deadline
    );

    Optional<PaymentTransaction> findByProviderCheckoutToken(String providerPaymentId);
}