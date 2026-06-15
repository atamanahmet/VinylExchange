package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.config.json.PriceKurusDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

/**
 * Accepts either buyer price or seller earnings, never both, never neither
 * Backend derives the other two values from whichever is provided
 */
public record PricePreviewRequest(

        @Min(value = 1, message = "Price must be at least 1 kurus")
        @JsonProperty("price")
        @JsonDeserialize(using = PriceKurusDeserializer.class)
        Long priceKurus,

        @Min(value = 1, message = "Seller earnings must be at least 1 kurus")
        @JsonProperty("sellerEarnings")
        @JsonDeserialize(using = PriceKurusDeserializer.class)
        Long sellerEarningsKurus
) {
    /**
     * Exactly one of the two fields must be non-null
     */
    @AssertTrue(message = "Provide exactly one of price or sellerEarnings, not both and not neither")
    @JsonIgnore
    public boolean isPriceInputValid() {
        return (priceKurus != null) != (sellerEarningsKurus != null);
    }
}