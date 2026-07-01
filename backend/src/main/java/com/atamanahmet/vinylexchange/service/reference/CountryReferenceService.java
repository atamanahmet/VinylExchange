package com.atamanahmet.vinylexchange.service.reference;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.enums.Country;
import com.atamanahmet.vinylexchange.dto.reference.EnumOptionDto;

@Service
public class CountryReferenceService {

    @Cacheable(cacheNames = "countryOptions", key = "#locale.toLanguageTag()")
    public List<EnumOptionDto> getCountryOptions(Locale locale) {
        return Arrays.stream(Country.values())
                .sorted(Comparator
                        .comparing(Country::getTier)
                        .thenComparing(country -> country.getDisplayName(locale), String.CASE_INSENSITIVE_ORDER))
                .map(country -> new EnumOptionDto(
                        country.getIsoCode(),
                        country.getDisplayName(locale),
                        country.getTier()))
                .toList();
    }
}
