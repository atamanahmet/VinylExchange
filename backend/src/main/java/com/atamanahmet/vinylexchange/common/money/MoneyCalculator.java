package com.atamanahmet.vinylexchange.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyCalculator {

    private static final int BASIS_POINT_DIVIDE = 10_000;
    private static final int MAX_DISCOUNT_BP = 10_000;

    private MoneyCalculator() {

    }

    public static long discounted(long priceKurus, int discountBP) {

        if (priceKurus < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative: " + priceKurus);
        }

        if (discountBP < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot be negative: " + discountBP);
        }

        if (discountBP == 0) {
            return priceKurus;
        }

        if (discountBP > MAX_DISCOUNT_BP) {
            throw new IllegalArgumentException(
                    "Discount cannot exceed 100% (10000 basis points): " + discountBP);
        }

        BigDecimal price = BigDecimal.valueOf(priceKurus);

        BigDecimal discount = BigDecimal.valueOf(discountBP)
                .divide(BigDecimal.valueOf(BASIS_POINT_DIVIDE), 4, RoundingMode.HALF_UP);

        BigDecimal result = price.multiply(BigDecimal.ONE.subtract(discount));

        return result.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
