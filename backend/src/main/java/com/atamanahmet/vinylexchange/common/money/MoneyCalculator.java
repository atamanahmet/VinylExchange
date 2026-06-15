package com.atamanahmet.vinylexchange.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyCalculator {

    private static final int BASIS_POINT_DIVIDE = 10_000;
    private static final int MAX_DISCOUNT_BP = 10_000;

    private MoneyCalculator() {

    }

    /**
     * Applies discount to price
     * discountBP: basis points, 1000 = 10%
     */
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

    /**
     * Calculates what buyer pays given seller's desired earnings
     * Rounds UP, buyer absorbs remainder, seller always gets what they asked for
     * Example: sellerEarnings=10000 kurus, feeBP=1000 (10%) → 11000 kurus
     */
    public static long buyerPrice(long sellerEarningsKurus, int feeBP) {
        validateFeeInputs(sellerEarningsKurus, feeBP);

        return BigDecimal.valueOf(sellerEarningsKurus)
                .multiply(BigDecimal.valueOf(BASIS_POINT_DIVIDE + feeBP))
                .divide(BigDecimal.valueOf(BASIS_POINT_DIVIDE), 0, RoundingMode.CEILING)
                .longValue();
    }

    /**
     * Calculates seller earnings from buyer price
     * Rounds DOWN, seller gets floor, never overpaid
     * Used for migrating existing listings where only buyer price is known
     */
    public static long sellerEarnings(long buyerPriceKurus, int feeBP) {
        validateFeeInputs(buyerPriceKurus, feeBP);

        return BigDecimal.valueOf(buyerPriceKurus)
                .multiply(BigDecimal.valueOf(BASIS_POINT_DIVIDE))
                .divide(BigDecimal.valueOf(BASIS_POINT_DIVIDE + feeBP), 0, RoundingMode.FLOOR)
                .longValue();
    }

    /**
     * Calculates platform cut from buyer price
     * platformCut + sellerEarnings = buyerPrice always holds true
     */
    public static long platformCut(long buyerPriceKurus, int feeBP) {
        validateFeeInputs(buyerPriceKurus, feeBP);
        return buyerPriceKurus - sellerEarnings(buyerPriceKurus, feeBP);
    }

    private static void validateFeeInputs(long amountKurus, int feeBP) {
        if (amountKurus < 0) throw new IllegalArgumentException("Amount cannot be negative: " + amountKurus);
        if (feeBP < 0) throw new IllegalArgumentException("Fee cannot be negative: " + feeBP);
        if (feeBP > MAX_DISCOUNT_BP) throw new IllegalArgumentException(
                "Fee cannot exceed 100%% (10000 basis points): " + feeBP);
    }
}
