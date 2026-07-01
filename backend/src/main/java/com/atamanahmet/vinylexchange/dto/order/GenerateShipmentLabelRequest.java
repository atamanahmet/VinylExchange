package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateShipmentLabelRequest(
        @NotBlank(message = "handlerCode cannot be blank")
        String handlerCode,
        @NotNull(message = "sellerAddressId is required")
        UUID sellerAddressId) {
}
