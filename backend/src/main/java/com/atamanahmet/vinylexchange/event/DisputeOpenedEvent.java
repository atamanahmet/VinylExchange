package com.atamanahmet.vinylexchange.event;

import com.atamanahmet.vinylexchange.domain.enums.DisputeReason;

import java.util.UUID;

/**
 * Published by CancelService when buyer opens a dispute
 * PaymentService listens to hold funds during dispute
 */
public record DisputeOpenedEvent(
        UUID orderId,
        DisputeReason reason
) {}