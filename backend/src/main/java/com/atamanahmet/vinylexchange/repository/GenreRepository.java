package com.atamanahmet.vinylexchange.repository;

import java.util.Optional;

import com.atamanahmet.vinylexchange.domain.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);
}
