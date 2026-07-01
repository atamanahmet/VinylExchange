package com.atamanahmet.vinylexchange.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.atamanahmet.vinylexchange.domain.enums.CountryTier;
import com.atamanahmet.vinylexchange.dto.reference.EnumOptionDto;
import com.atamanahmet.vinylexchange.dto.reference.GenreOptionDto;
import com.atamanahmet.vinylexchange.service.reference.CountryReferenceService;
import com.atamanahmet.vinylexchange.service.reference.GenreReferenceService;

@ExtendWith(MockitoExtension.class)
class ReferenceControllerTest {

    @Mock
    private CountryReferenceService countryReferenceService;

    @Mock
    private GenreReferenceService genreReferenceService;

    @InjectMocks
    private ReferenceController referenceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(referenceController).build();
    }

    @Test
    void getCountries_withoutLang_defaultsToTurkishLocale() throws Exception {
        when(countryReferenceService.getCountryOptions(Locale.forLanguageTag("tr")))
                .thenReturn(List.of(new EnumOptionDto("TR", "Türkiye", CountryTier.PRIMARY)));

        mockMvc.perform(get("/api/reference/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("TR"))
                .andExpect(jsonPath("$[0].label").value("Türkiye"));

        verify(countryReferenceService).getCountryOptions(Locale.forLanguageTag("tr"));
    }

    @Test
    void getCountries_withLang_usesRequestedLocale() throws Exception {
        when(countryReferenceService.getCountryOptions(Locale.forLanguageTag("en")))
                .thenReturn(List.of(new EnumOptionDto("TR", "Turkey", CountryTier.PRIMARY)));

        mockMvc.perform(get("/api/reference/countries").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Turkey"));

        verify(countryReferenceService).getCountryOptions(eq(Locale.forLanguageTag("en")));
    }

    @Test
    void getCountries_withInvalidLang_defaultsToTurkishLocale() throws Exception {
        when(countryReferenceService.getCountryOptions(Locale.forLanguageTag("tr")))
                .thenReturn(List.of(new EnumOptionDto("TR", "Türkiye", CountryTier.PRIMARY)));

        mockMvc.perform(get("/api/reference/countries").param("lang", "!!!"))
                .andExpect(status().isOk());

        verify(countryReferenceService).getCountryOptions(Locale.forLanguageTag("tr"));
    }

    @Test
    void getGenres_withoutParams_defaultsToTurkishLocaleAndIncludeLocal() throws Exception {
        when(genreReferenceService.getGenreOptions(Locale.forLanguageTag("tr"), true))
                .thenReturn(List.of(new GenreOptionDto(1L, "Rock", null, false)));

        mockMvc.perform(get("/api/reference/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].label").value("Rock"))
                .andExpect(jsonPath("$[0].parentId").isEmpty())
                .andExpect(jsonPath("$[0].localFlavor").value(false));

        verify(genreReferenceService).getGenreOptions(Locale.forLanguageTag("tr"), true);
    }

    @Test
    void getGenres_withIncludeLocalFalse_excludesRegionalGenres() throws Exception {
        when(genreReferenceService.getGenreOptions(Locale.forLanguageTag("en"), false))
                .thenReturn(List.of(new GenreOptionDto(2L, "Rock", null, false)));

        mockMvc.perform(get("/api/reference/genres")
                        .param("lang", "en")
                        .param("includeLocal", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Rock"));

        verify(genreReferenceService).getGenreOptions(eq(Locale.forLanguageTag("en")), eq(false));
    }
}
