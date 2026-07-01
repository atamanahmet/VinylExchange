package com.atamanahmet.vinylexchange.dto.payment;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentHistoryDto(
        UUID paymentTransactionId,
        UUID orderId,
        Long orderNumber,
        PaymentStatus status,
        Long amountKurus,
        LocalDateTime capturedAt,
        LocalDateTime createdAt,
        boolean refundReviewRequired,
        String sellerUsername,
        List<PaymentHistoryEventDto> events) {
}
