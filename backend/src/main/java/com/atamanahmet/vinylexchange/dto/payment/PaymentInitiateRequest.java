package com.atamanahmet.vinylexchange.dto.payment;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentInitiateRequest(
        @NotNull UUID orderId
) {}