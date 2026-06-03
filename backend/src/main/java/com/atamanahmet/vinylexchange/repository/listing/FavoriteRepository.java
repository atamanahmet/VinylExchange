package com.atamanahmet.vinylexchange.repository.listing;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    boolean existsByUserIdAndListingId(UUID userId, UUID listingId);

    void deleteByUserIdAndListingId(UUID userId, UUID listingId);

    List<Favorite> findAllByUserId(UUID userId);
}
