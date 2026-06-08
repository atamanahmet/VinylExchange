package com.atamanahmet.vinylexchange.dto.order;

import java.time.LocalDateTime;

public record PaymentSummaryDTO(
        String providerPaymentId,
        String providerInternalPaymentId,
        String authCode,
        String hostReference,
        Integer fraudStatus,
        LocalDateTime capturedAt
) {}
