package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.common.money.ListingPriceResult;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Returned to frontend for both price preview and listing responses
 * All kurus fields serialized as TL for display
 */
public record ListingPriceResultDTO(

        @JsonSerialize(using = PriceTlSerializer.class)
        long priceKurus,

        @JsonSerialize(using = PriceTlSerializer.class)
        long sellerEarningsKurus,

        @JsonSerialize(using = PriceTlSerializer.class)
        long platformCutKurus,

        int feeBP
) {
    /**
     * Converts internal calculation result to frontend DTO
     */
    public static ListingPriceResultDTO from(ListingPriceResult result) {
        return new ListingPriceResultDTO(
                result.priceKurus(),
                result.sellerEarningsKurus(),
                result.platformCutKurus(),
                result.feeBP()
        );
    }
}