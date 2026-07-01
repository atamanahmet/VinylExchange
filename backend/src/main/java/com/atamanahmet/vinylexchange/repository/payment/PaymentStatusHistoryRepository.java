package com.atamanahmet.vinylexchange.repository.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Audit log repository, write only from application code.
 * Read directly from DB for admin inspection.
 */
public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, UUID> {

    @Query("""
            SELECT h FROM PaymentStatusHistory h
            JOIN FETCH h.paymentTransaction pt
            WHERE pt.id IN :paymentIds
            ORDER BY h.occurredAt ASC
            """)
    List<PaymentStatusHistory> findAllByPaymentTransactionIdInOrderByOccurredAtAsc(
            @Param("paymentIds") Collection<UUID> paymentIds);
}