package com.atamanahmet.vinylexchange.service.reference;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.atamanahmet.vinylexchange.domain.enums.Country;
import com.atamanahmet.vinylexchange.domain.enums.CountryTier;
import com.atamanahmet.vinylexchange.dto.reference.EnumOptionDto;

class CountryReferenceServiceTest {

    private final CountryReferenceService service = new CountryReferenceService();

    @Test
    void getCountryOptions_turkishLocale_putsTurkeyFirst() {
        List<EnumOptionDto> options = service.getCountryOptions(Locale.forLanguageTag("tr"));

        assertThat(options).isNotEmpty();
        assertThat(options.get(0).value()).isEqualTo("TR");
        assertThat(options.get(0).label()).isEqualTo("Türkiye");
    }

    @Test
    void getCountryOptions_sortsByTierThenLocalizedName() {
        List<EnumOptionDto> options = service.getCountryOptions(Locale.ENGLISH);

        assertThat(options.get(0).value()).isEqualTo("TR");
        assertThat(Country.fromIsoCode(options.get(1).value()).getTier()).isEqualTo(CountryTier.COMMON);
        assertThat(Country.fromIsoCode(options.get(6).value()).getTier()).isEqualTo(CountryTier.STANDARD);

        List<String> commonValues = options.subList(1, 6).stream().map(EnumOptionDto::value).toList();
        assertThat(commonValues).containsExactlyInAnyOrder("FR", "DE", "JP", "GB", "US");

        List<String> commonLabels = options.subList(1, 6).stream().map(EnumOptionDto::label).toList();
        assertThat(commonLabels).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
    }

    @Test
    void getCountryOptions_mapsValueToEnumNameAndLabelToDisplayName() {
        EnumOptionDto turkey = service.getCountryOptions(Locale.ENGLISH).stream()
                .filter(option -> option.value().equals("TR"))
                .findFirst()
                .orElseThrow();

        assertThat(turkey.label()).isEqualTo(Country.TURKEY.getDisplayName(Locale.ENGLISH));
    }
}
