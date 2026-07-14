package com.atamanahmet.vinylexchange.dto.listing;

public record ListingSummaryDto(
        String publicId,
        String title,
        String artistName,
        long priceKurus,
        String mainImageUrl,
        String condition,
        int year,
        String country,
        String format,
        String labelName) {
}
