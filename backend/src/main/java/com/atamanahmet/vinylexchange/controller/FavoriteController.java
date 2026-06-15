package com.atamanahmet.vinylexchange.controller;

import java.util.Set;
import java.util.UUID;

import com.atamanahmet.vinylexchange.service.listing.FavoriteService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import com.atamanahmet.vinylexchange.dto.listing.FavoriteRequest;

@RestController
@RequestMapping("/api/favorites/")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<?> getFavorites() {

        Set<UUID> favorites = favoriteService.getUserFavorites(UserUtil.getCurrentUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(favorites);
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestBody FavoriteRequest request) {

        favoriteService.addToFavorites(UserUtil.getCurrentUserId(), request.listingId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable(name = "listingId", required = true) UUID listingId) {

        favoriteService.removeFromFavorites(UserUtil.getCurrentUserId(), listingId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
