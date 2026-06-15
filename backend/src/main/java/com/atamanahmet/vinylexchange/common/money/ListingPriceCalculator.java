package com.atamanahmet.vinylexchange.common.money;

import com.atamanahmet.vinylexchange.config.PlatformFeeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Single entry point for all listing price calculations
 * Seller inputs either value, this class derives the other two
 */
@Component
@RequiredArgsConstructor
public class ListingPriceCalculator {

    private static final int DISCOUNT_QUALIFYING_DAYS = 30;

    private final PlatformFeeProperties feeProperties;

    /**
     * Seller inputs what they want to receive
     * Buyer price is ceiling so seller always gets exactly what they asked for
     */
    public ListingPriceResult fromSellerEarnings(long sellerEarningsKurus) {
        int feeBP = feeProperties.getBp();
        long buyerPrice = MoneyCalculator.buyerPrice(sellerEarningsKurus, feeBP);
        long platformCut = buyerPrice - sellerEarningsKurus;
        return new ListingPriceResult(buyerPrice, sellerEarningsKurus, platformCut, feeBP);
    }

    /**
     * Seller inputs what buyer will pay
     * Seller earnings and platform cut derived from that
     */
    public ListingPriceResult fromBuyerPrice(long buyerPriceKurus) {
        int feeBP = feeProperties.getBp();
        long sellerEarnings = MoneyCalculator.sellerEarnings(buyerPriceKurus, feeBP);
        long platformCut = MoneyCalculator.platformCut(buyerPriceKurus, feeBP);
        return new ListingPriceResult(buyerPriceKurus, sellerEarnings, platformCut, feeBP);
    }

    /**
     * Calculates buyer-visible discount percent from original and current price
     * Returns empty if price never changed, price went up, or 30 days have not passed since last change
     * priceLastChangedAt null means price was never changed, no discount qualifies
     */
    public Optional<Integer> discountPercent(
            long originalPriceKurus,
            long currentPriceKurus,
            LocalDateTime priceLastChangedAt) {

        if (priceLastChangedAt == null) {
            return Optional.empty();
        }

        boolean thirtyDaysPassed = priceLastChangedAt
                .plusDays(DISCOUNT_QUALIFYING_DAYS)
                .isBefore(LocalDateTime.now());

        if (!thirtyDaysPassed) {
            return Optional.empty();
        }

        if (currentPriceKurus >= originalPriceKurus) {
            return Optional.empty();
        }

        int percent = (int) Math.round(
                (double) (originalPriceKurus - currentPriceKurus) / originalPriceKurus * 100);

        return Optional.of(percent);
    }
}