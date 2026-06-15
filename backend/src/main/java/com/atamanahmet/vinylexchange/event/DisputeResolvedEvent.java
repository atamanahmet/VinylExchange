package com.atamanahmet.vinylexchange.event;

import java.util.UUID;

/**
 * Published by CancelService when admin resolves a dispute
 * PaymentService listens to release or refund based on resolution
 */
public record DisputeResolvedEvent(
        UUID orderId,
        Resolution resolution
) {
    public enum Resolution {
        FOR_SELLER,
        FOR_BUYER
    }
}