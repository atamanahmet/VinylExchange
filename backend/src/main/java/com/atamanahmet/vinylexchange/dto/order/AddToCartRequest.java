package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull UUID listingId,
        @Min(1) int quantity) {
}