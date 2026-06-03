package com.atamanahmet.vinylexchange.repository.listing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    Optional<WishlistItem> findByIdAndUserId(UUID WishlistItemId, UUID userId);

    Optional<WishlistItem> findByUserIdAndTitleAndArtist(UUID userId, String title,
            String artist);

    List<WishlistItem> findByUserId(UUID userId);

    List<WishlistItem> findByTitle(String title);

    List<WishlistItem> findByArtist(String artist);

    @Query("SELECT w FROM WishlistItem w " +
            "WHERE LOWER(w.title) = LOWER(:title) " +
            "AND LOWER(w.artist) = LOWER(:artist)")
    List<WishlistItem> findByTitleAndArtist(String title, String artist);

    boolean existsByUserIdAndTitleAndArtist(UUID userId, String title, String artist);

    boolean existsByUserIdAndTitleAndArtistAndYear(UUID userId, String title, String artist, Integer year);

    boolean existsByUserIdAndTitle(UUID userId, String title);

    boolean existsByUserIdAndArtist(UUID userId, String artist);

    // TODO: check if necessary, delete by wl id must be suffice
    void deleteByUserIdAndTitleAndArtist(UUID userId, String title, String artist);
}
