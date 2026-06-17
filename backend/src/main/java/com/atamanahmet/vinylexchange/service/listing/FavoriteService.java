package com.atamanahmet.vinylexchange.service.listing;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.domain.entity.Favorite;
import com.atamanahmet.vinylexchange.repository.listing.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public void addToFavorites(UUID userId, UUID listingId) {

        boolean isExist = favoriteRepository.existsByUserIdAndListingId(userId, listingId);

        if (!isExist) {

            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .listingId(listingId)
                    .build();

            favoriteRepository.save(favorite);
        }
    }

    public void removeFromFavorites(UUID userId, UUID listingId) {

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    public Set<UUID> getUserFavorites(UUID userId) {

        return favoriteRepository.findAllByUserId(userId) // list
                .stream()
                .map(favorite -> favorite.getListingId())
                .collect(Collectors.toSet());
    }

    public boolean isFavorited(UUID userId, UUID listingId) {

        return favoriteRepository.existsByUserIdAndListingId(userId, listingId);
    }

}
