package com.atamanahmet.vinylexchange.dto.order;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record PricePreviewRequest(
        @NotNull @Digits(integer = 10, fraction = 2) @DecimalMin(value = "0.01", message = "Price must be at least 0.01 TL") @DecimalMax(value = "99999999.99", message = "Price exceeds maximum allowed") BigDecimal priceTL,

        @NotNull @DecimalMin(value = "0.00", inclusive = true) @DecimalMax(value = "100.00", inclusive = true) BigDecimal discountPercent) {

}
