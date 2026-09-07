package com.atamanahmet.vinylexchange.controller;

import java.util.Set;

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

        Set<String> favorites = favoriteService.getUserFavorites(UserUtil.getCurrentUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(favorites);
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestBody FavoriteRequest request) {

        favoriteService.addToFavorites(UserUtil.getCurrentUserId(), request.publicId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable(name = "publicId", required = true) String publicId) {

        favoriteService.removeFromFavorites(UserUtil.getCurrentUserId(), publicId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
