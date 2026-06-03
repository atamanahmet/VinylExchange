package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
                @NotNull UUID listingId,
                @Min(1) int quantity) {

}