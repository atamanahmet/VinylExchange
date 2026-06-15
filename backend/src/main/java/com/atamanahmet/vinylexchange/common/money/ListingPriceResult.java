package com.atamanahmet.vinylexchange.common.money;

/**
 * Carries all three price values calculated from one input
 * Immutable, no logic
 */
public record ListingPriceResult(
        long priceKurus,
        long sellerEarningsKurus,
        long platformCutKurus,
        int feeBP
) {}