package com.atamanahmet.vinylexchange.event;

import java.util.UUID;

/**
 * Published when buyer cancels from PAID status
 * PaymentService listens to issue refund
 */
public record OrderCancelledEvent(UUID orderId) {}