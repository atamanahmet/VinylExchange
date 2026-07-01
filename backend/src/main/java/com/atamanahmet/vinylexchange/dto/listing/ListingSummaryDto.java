package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record ListingSummaryDto(
        String publicId,
        String title,
        String artistName,
        @JsonProperty("price")
        @JsonSerialize(using = PriceTlSerializer.class)
        long priceKurus,
        String mainImageUrl,
        String condition,
        int year,
        String country,
        String format,
        String labelName) {
}
