package com.atamanahmet.vinylexchange.dto.reference;

import com.atamanahmet.vinylexchange.domain.enums.CountryTier;

public record EnumOptionDto(String value, String label, CountryTier tier) {
}
