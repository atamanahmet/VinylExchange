package com.atamanahmet.vinylexchange.dto.order;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
        @Min(1) int quantity) {
}