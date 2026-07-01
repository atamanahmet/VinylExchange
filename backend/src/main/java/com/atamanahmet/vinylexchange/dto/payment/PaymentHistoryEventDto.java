package com.atamanahmet.vinylexchange.dto.payment;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentHistoryEventDto(
        PaymentStatus fromStatus,
        PaymentStatus toStatus,
        LocalDateTime occurredAt,
        String note) {
}
