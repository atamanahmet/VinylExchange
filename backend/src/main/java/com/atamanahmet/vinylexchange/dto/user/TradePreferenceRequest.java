package com.atamanahmet.vinylexchange.dto.user;

import com.atamanahmet.vinylexchange.domain.PaymentDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TradePreferenceRequest(
        @NotBlank(message = "Desired item cannot be empty")
        @Size(max = 200, message = "Desired item name must not exceed 200 characters")
        String desiredItem,
        Double extraAmount,
        @NotNull(message = "Payment direction is required")
        PaymentDirection paymentDirection
) {
    public TradePreferenceRequest {
        if (extraAmount == null) {
            extraAmount = 0.0;
        }
        if (paymentDirection == null) {
            paymentDirection = PaymentDirection.NO_EXTRA;
        }
    }
}
