package com.atamanahmet.vinylexchange.service.listing;

import com.atamanahmet.vinylexchange.common.money.ListingPriceCalculator;
import com.atamanahmet.vinylexchange.dto.listing.ListingPriceResultDTO;
import com.atamanahmet.vinylexchange.dto.listing.PricePreviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Calculates and returns all three price values for a given input
 * Used by frontend to keep both price fields in sync as seller types
 */
@Service
@RequiredArgsConstructor
public class PricePreviewService {

        private final ListingPriceCalculator priceCalculator;

        /**
         * Accepts either buyer price or seller earnings
         * Returns all three values calculated by ListingPriceCalculator
         */
        public ListingPriceResultDTO preview(PricePreviewRequest request) {
                if (request.sellerEarningsKurus() != null) {
                        return ListingPriceResultDTO.from(
                                priceCalculator.fromSellerEarnings(request.sellerEarningsKurus()));
                }
                return ListingPriceResultDTO.from(
                        priceCalculator.fromBuyerPrice(request.priceKurus()));
        }
}