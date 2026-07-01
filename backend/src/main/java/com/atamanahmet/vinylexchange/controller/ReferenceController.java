package com.atamanahmet.vinylexchange.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.reference.EnumOptionDto;
import com.atamanahmet.vinylexchange.dto.reference.GenreOptionDto;
import com.atamanahmet.vinylexchange.service.reference.CountryReferenceService;
import com.atamanahmet.vinylexchange.service.reference.GenreReferenceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
public class ReferenceController {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("tr");

    private final CountryReferenceService countryReferenceService;
    private final GenreReferenceService genreReferenceService;

    @GetMapping("/countries")
    public ResponseEntity<List<EnumOptionDto>> getCountries(
            @RequestParam(required = false) String lang) {
        Locale locale = resolveLocale(lang);
        return ResponseEntity.ok(countryReferenceService.getCountryOptions(locale));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<GenreOptionDto>> getGenres(
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "true") boolean includeLocal) {
        Locale locale = resolveLocale(lang);
        return ResponseEntity.ok(genreReferenceService.getGenreOptions(locale, includeLocal));
    }

    private Locale resolveLocale(String lang) {
        if (lang == null || lang.isBlank()) {
            return DEFAULT_LOCALE;
        }
        Locale locale = Locale.forLanguageTag(lang.trim());
        if (locale.getLanguage().isBlank()) {
            return DEFAULT_LOCALE;
        }
        return locale;
    }
}
