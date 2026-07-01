package com.atamanahmet.vinylexchange.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.entity.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);

    @EntityGraph(attributePaths = "parent")
    @Query("SELECT g FROM Genre g")
    List<Genre> findAllWithParent();
}
