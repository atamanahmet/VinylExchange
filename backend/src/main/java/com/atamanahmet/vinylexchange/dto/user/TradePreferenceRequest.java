package com.atamanahmet.vinylexchange.dto.user;

import com.atamanahmet.vinylexchange.domain.PaymentDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TradePreferenceRequest {

    @NotBlank(message = "Desired item cannot be empty")
    @Size(max = 200, message = "Desired item name must not exceed 200 characters")
    private String desiredItem;

    private Double extraAmount = 0.0;

    @NotNull(message = "Payment direction is required")
    private PaymentDirection paymentDirection = PaymentDirection.NO_EXTRA;
}
