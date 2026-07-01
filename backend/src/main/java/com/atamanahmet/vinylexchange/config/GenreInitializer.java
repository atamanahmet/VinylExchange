package com.atamanahmet.vinylexchange.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.entity.Genre;
import com.atamanahmet.vinylexchange.repository.GenreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs on every startup after roles, before demo data init.
 * Seeds curated genre taxonomy into DB if not present.
 */
@Component
@Slf4j
@Order(2)
@RequiredArgsConstructor
public class GenreInitializer implements ApplicationRunner {

        private final GenreRepository genreRepository;

        @Override
        @Transactional
        public void run(ApplicationArguments args) {
                log.info("Initializing genres...");

                Set<String> existingNames = genreRepository.findAll().stream()
                                .map(Genre::getName)
                                .collect(Collectors.toCollection(HashSet::new));

                Map<String, Genre> topLevelByName = new HashMap<>();
                int createdCount = 0;

                for (GenreCatalog.GenreSeed seed : GenreCatalog.ALL) {
                        if (seed.parentName() != null) {
                                continue;
                        }

                        if (existingNames.contains(seed.name())) {
                                genreRepository.findByName(seed.name())
                                                .ifPresent(genre -> topLevelByName.put(seed.name(), genre));
                                log.info("Genre already exists: {}", seed.name());
                                continue;
                        }

                        Genre genre = new Genre();
                        genre.setName(seed.name());
                        genre.setFeatured(true);
                        genre.setLocalFlavor(seed.localFlavor());
                        Genre saved = genreRepository.save(genre);
                        topLevelByName.put(seed.name(), saved);
                        existingNames.add(seed.name());
                        createdCount++;
                        log.info("Genre created: {}", seed.name());
                }

                for (GenreCatalog.GenreSeed seed : GenreCatalog.ALL) {
                        if (seed.parentName() == null) {
                                continue;
                        }

                        if (existingNames.contains(seed.name())) {
                                log.info("Genre already exists: {}", seed.name());
                                continue;
                        }

                        Genre parent = topLevelByName.get(seed.parentName());
                        Genre genre = new Genre();
                        genre.setName(seed.name());
                        genre.setFeatured(false);
                        genre.setLocalFlavor(seed.localFlavor());
                        genre.setParent(parent);
                        genreRepository.save(genre);
                        existingNames.add(seed.name());
                        createdCount++;
                        log.info("Genre created: {}", seed.name());
                }

                log.info("Genre initialization completed. created={}", createdCount);
        }
}
