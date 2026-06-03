package com.atamanahmet.vinylexchange.service;

import com.atamanahmet.vinylexchange.domain.entity.Genre;
import com.atamanahmet.vinylexchange.repository.GenreRepository;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.exception.GenreNotFoundException;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Genre saveGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public Genre findGenreByName(String name) {
        return genreRepository.findByName(name).orElseThrow(() -> new GenreNotFoundException());
    }
}
