package com.atamanahmet.vinylexchange.dto.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

public record RefundReviewPaymentDto(
        UUID orderId,
        UUID paymentId,
        Long amount,
        LocalDateTime capturedAt,
        PaymentStatus status
) {
    public static RefundReviewPaymentDto from(PaymentTransaction payment) {
        return new RefundReviewPaymentDto(
                payment.getOrder().getId(),
                payment.getId(),
                payment.getAmountKurus(),
                payment.getCapturedAt(),
                payment.getStatus());
    }
}
