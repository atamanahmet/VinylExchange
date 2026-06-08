package com.atamanahmet.vinylexchange.dto.payment;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentStatusDTO(
        UUID paymentTransactionId,
        UUID orderId,
        PaymentStatus status,
        Long amountKurus,
        LocalDateTime autoConfirmDeadline
) {
}