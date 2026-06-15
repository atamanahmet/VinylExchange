package com.atamanahmet.vinylexchange.event;

import com.atamanahmet.vinylexchange.domain.enums.SaleType;

import java.util.UUID;

/**
 * Published by CheckoutService after each seller order is created
 * PaymentService listens to initiate payment for FIXED_PRICE orders
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        SaleType saleType,
        Long amountKurus
) {}