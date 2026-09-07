package com.atamanahmet.vinylexchange.service.listing;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.domain.entity.Favorite;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.exception.ListingNotFoundException;
import com.atamanahmet.vinylexchange.repository.listing.FavoriteRepository;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    public void addToFavorites(UUID userId, String publicId) {
        UUID listingId = listingRepository.findByPublicId(publicId)
                .orElseThrow(ListingNotFoundException::new)
                .getId();

        boolean isExist = favoriteRepository.existsByUserIdAndListingId(userId, listingId);

        if (!isExist) {

            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .listingId(listingId)
                    .build();

            favoriteRepository.save(favorite);
        }
    }

    public void removeFromFavorites(UUID userId, String publicId) {
        UUID listingId = listingRepository.findByPublicId(publicId)
                .orElseThrow(ListingNotFoundException::new)
                .getId();

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    public Set<String> getUserFavorites(UUID userId) {

        List<UUID> listingIds = favoriteRepository.findAllByUserId(userId).stream()
                .map(Favorite::getListingId)
                .toList();

        return listingRepository.findAllByIdIn(listingIds).stream()
                .map(Listing::getPublicId)
                .collect(Collectors.toSet());
    }

    public boolean isFavorited(UUID userId, UUID listingId) {

        return favoriteRepository.existsByUserIdAndListingId(userId, listingId);
    }

}
