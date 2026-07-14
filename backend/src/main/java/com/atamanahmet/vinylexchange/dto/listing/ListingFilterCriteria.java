package com.atamanahmet.vinylexchange.dto.listing;

import java.util.Collection;
import java.util.List;

import com.atamanahmet.vinylexchange.domain.enums.Country;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

public record ListingFilterCriteria(
        List<Country> country,
        List<MediaFormat> format,
        List<Integer> speedRpm,
        List<VinylSubtype> vinylSubtype,
        List<String> condition,
        Integer yearFrom,
        Integer yearTo,
        List<Long> genreIds,
        Boolean tradeable,
        Long priceFromKurus,
        Long priceToKurus,
        String ownerUsername) {

    /** True when no browse filters set — safe for unfiltered Redis cache path. */
    public boolean isEmpty() {
        return isBlank(country)
                && isBlank(format)
                && isBlank(speedRpm)
                && isBlank(vinylSubtype)
                && isBlank(condition)
                && yearFrom == null
                && yearTo == null
                && isBlank(genreIds)
                && tradeable == null
                && priceFromKurus == null
                && priceToKurus == null
                && (ownerUsername == null || ownerUsername.isBlank());
    }

    private static boolean isBlank(Collection<?> values) {
        return values == null || values.isEmpty();
    }
}
