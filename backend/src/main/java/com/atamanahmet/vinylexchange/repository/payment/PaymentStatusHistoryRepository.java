package com.atamanahmet.vinylexchange.repository.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Audit log repository, write only from application code.
 * Read directly from DB for admin inspection.
 */
public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, UUID> {
}