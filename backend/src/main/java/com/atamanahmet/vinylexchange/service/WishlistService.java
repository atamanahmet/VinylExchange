package com.atamanahmet.vinylexchange.service;

import com.atamanahmet.vinylexchange.repository.listing.WishlistItemRepository;
import com.atamanahmet.vinylexchange.domain.entity.WishlistItem;
import com.atamanahmet.vinylexchange.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.exception.ResourceNotFoundException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistBulkRequest;
import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistRequest;
import com.atamanahmet.vinylexchange.dto.listing.WishlistItemDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistItemRepository wishlistItemRepository;
    private final UserService userService;

    public List<WishlistItemDTO> getWishlistByUserId(UUID userId) {
        List<WishlistItem> wishlists = wishlistItemRepository.findByUserId(userId);
        return wishlists.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<WishlistItemDTO> addAllToWishlist(UUID userId, AddToWishlistBulkRequest requests) {

        List<WishlistItemDTO> wishlistDTO = new ArrayList<>();

        for (AddToWishlistRequest eachRequest : requests.getBulkRequest()) {

            try {
                WishlistItemDTO itemDTO = addToWishlist(userId, eachRequest);

                wishlistDTO.add(itemDTO);
            } catch (Exception e) {
                logger.warn("Duplicate wishlist item for: {}- reason: {}", eachRequest.getTitle(), e.getMessage());
            }
        }

        return wishlistDTO;
    }

    // checks item year only while creation,
    // notification will send even when release year is diffirent
    @Transactional
    public WishlistItemDTO addToWishlist(UUID userId, AddToWishlistRequest request) {

        System.err.println(request.getExternalCoverUrl());

        String title = request.getTitle();
        String artist = request.getArtist();
        Integer year = request.getYear();

        if (wishlistItemRepository.existsByUserIdAndTitleAndArtistAndYear(
                userId, title, artist, year)) {

            throw new RuntimeException("This item is already in your wishlist");
        }

        User user = userService.findByUserId(userId);

        WishlistItem wishlistItem = new WishlistItem();

        wishlistItem.setUser(user);
        wishlistItem.setTitle(title);
        wishlistItem.setArtist(artist);
        wishlistItem.setYear(year);
        wishlistItem.setCountry(request.getCountry());
        wishlistItem.setLabel(request.getLabel());
        wishlistItem.setBarcode(request.getBarcode());
        wishlistItem.setExternalCoverUrl(request.getExternalCoverUrl());
        wishlistItem.setFormat(request.getFormat());

        WishlistItem savedWishlistItem = wishlistItemRepository.save(wishlistItem);

        return convertToDTO(savedWishlistItem);
    }

    @Transactional
    public void removeFromWishlist(UUID userId, UUID wishlistItemId) {
        WishlistItem wishlistItem = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));

        User user = userService.findByUserId(userId);

        if (!wishlistItem.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("User don't have permission to delete this wishlist item");
        }

        wishlistItemRepository.delete(wishlistItem);

    }

    public boolean isInWishlist(UUID userId, String title, String artist, Integer year) {

        return wishlistItemRepository.existsByUserIdAndTitleAndArtistAndYear(
                userId, title, artist, year);
    }

    // no year check, send notification
    public List<WishlistItem> findMatchingWishlists(String title, String artist) {

        if (title == null || artist == null) {
            return List.of();
        }
        return wishlistItemRepository.findByTitleAndArtist(title, artist);
    }

    private WishlistItemDTO convertToDTO(WishlistItem wishlistItem) {
        return WishlistItemDTO.builder()
                .id(wishlistItem.getId())
                .title(wishlistItem.getTitle())
                .artist(wishlistItem.getArtist())
                .year(wishlistItem.getYear())
                .country(wishlistItem.getCountry())
                .externalCoverUrl(wishlistItem.getExternalCoverUrl())
                .barcode(wishlistItem.getBarcode())
                .label(wishlistItem.getLabel())
                .format(wishlistItem.getFormat())
                .addedAt(wishlistItem.getAddedAt())
                .build();
    }
}
