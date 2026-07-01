package com.atamanahmet.vinylexchange.service.reference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.entity.Genre;
import com.atamanahmet.vinylexchange.dto.reference.GenreOptionDto;
import com.atamanahmet.vinylexchange.repository.GenreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreReferenceService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "genreOptions", key = "#locale.toLanguageTag() + '-' + #includeLocal")
    public List<GenreOptionDto> getGenreOptions(Locale locale, boolean includeLocal) {
        List<Genre> genres = genreRepository.findAllWithParent();

        List<Genre> filtered = genres.stream()
                .filter(genre -> includeLocal || !genre.isLocalFlavor())
                .toList();

        Map<Long, List<Genre>> childrenByParentId = filtered.stream()
                .filter(genre -> genre.getParent() != null)
                .collect(Collectors.groupingBy(genre -> genre.getParent().getId()));

        List<Genre> roots = filtered.stream()
                .filter(genre -> genre.getParent() == null)
                .sorted(Comparator.comparing(Genre::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<GenreOptionDto> options = new ArrayList<>();
        for (Genre root : roots) {
            options.add(toDto(root, locale));
            childrenByParentId.getOrDefault(root.getId(), List.of()).stream()
                    .sorted(Comparator.comparing(Genre::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(genre -> toDto(genre, locale))
                    .forEach(options::add);
        }

        return options;
    }

    private GenreOptionDto toDto(Genre genre, Locale locale) {
        return new GenreOptionDto(
                genre.getId(),
                resolveLabel(genre, locale),
                genre.getParent() != null ? genre.getParent().getId() : null,
                genre.isLocalFlavor());
    }

    private String resolveLabel(Genre genre, Locale locale) {
        return genre.getName();
    }
}
