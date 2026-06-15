package com.atamanahmet.vinylexchange.event;

import java.util.UUID;

/**
 * Published by OrderService when delivery is confirmed (buyer or auto)
 * PaymentService listens to release funds to seller
 */
public record OrderDeliveredEvent(UUID orderId) {}