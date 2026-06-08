package com.atamanahmet.vinylexchange.event;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;

import java.util.UUID;

/**
 * Published after every payment status transition
 * Used for notifications only, not for business logic
 */
public record PaymentStateChangedEvent(
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        PaymentStatus previousStatus,
        PaymentStatus newStatus
) {}