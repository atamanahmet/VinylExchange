package com.atamanahmet.vinylexchange.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class CountryTest {

    @Test
    void getDisplayName_usesLocaleAtCallTime() {
        assertThat(Country.TURKEY.getDisplayName(Locale.forLanguageTag("tr"))).isEqualTo("Türkiye");
        assertThat(Country.TURKEY.getDisplayName(Locale.ENGLISH)).isEqualTo("Turkey");
    }

    @Test
    void fromIsoCode_resolvesAlpha2Code() {
        assertThat(Country.fromIsoCode("tr")).isEqualTo(Country.TURKEY);
        assertThat(Country.fromIsoCode("GB")).isEqualTo(Country.UNITED_KINGDOM);
    }

    @Test
    void parse_acceptsEnumNameOrIsoCode() {
        assertThat(Country.parse("TURKEY")).isEqualTo(Country.TURKEY);
        assertThat(Country.parse("US")).isEqualTo(Country.UNITED_STATES);
    }

    @Test
    void tierOverrides_matchSpec() {
        assertThat(Country.TURKEY.getTier()).isEqualTo(CountryTier.PRIMARY);
        assertThat(Country.FRANCE.getTier()).isEqualTo(CountryTier.COMMON);
        assertThat(Country.GERMANY.getTier()).isEqualTo(CountryTier.COMMON);
        assertThat(Country.JAPAN.getTier()).isEqualTo(CountryTier.COMMON);
        assertThat(Country.UNITED_KINGDOM.getTier()).isEqualTo(CountryTier.COMMON);
        assertThat(Country.UNITED_STATES.getTier()).isEqualTo(CountryTier.COMMON);
        assertThat(Country.ANDORRA.getTier()).isEqualTo(CountryTier.STANDARD);
    }

    @Test
    void fromIsoCode_unknownCode_throws() {
        assertThatThrownBy(() -> Country.fromIsoCode("XX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XX");
    }
}
