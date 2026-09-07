package com.atamanahmet.vinylexchange.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddToCartRequest(
        @NotBlank String publicId,
        @Min(1) int quantity) {
}