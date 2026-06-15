package com.atamanahmet.vinylexchange.event;

import java.util.UUID;

/**
 * Published by OrderService when seller marks order as shipped
 * PaymentService listens to set autoConfirmDeadline
 */
public record OrderShippedEvent(UUID orderId) {}