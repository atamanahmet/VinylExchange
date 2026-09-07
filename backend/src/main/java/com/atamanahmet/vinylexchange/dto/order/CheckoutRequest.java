package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "shippingAddressId is required")
        UUID shippingAddressId) {
}
