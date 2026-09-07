package com.atamanahmet.vinylexchange.dto.listing;

import com.atamanahmet.vinylexchange.config.json.PriceTlSerializer;
import com.atamanahmet.vinylexchange.domain.entity.ListingPriceHistory;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;

public record ListingPriceHistoryDto(
        @JsonSerialize(using = PriceTlSerializer.class)
        Long oldPriceKurus,

        @JsonSerialize(using = PriceTlSerializer.class)
        long newPriceKurus,

        LocalDateTime occurredAt,

        String note) {

    public static ListingPriceHistoryDto from(ListingPriceHistory entry) {
        return new ListingPriceHistoryDto(
                entry.getOldPriceKurus(),
                entry.getNewPriceKurus(),
                entry.getOccurredAt(),
                entry.getNote());
    }
}
