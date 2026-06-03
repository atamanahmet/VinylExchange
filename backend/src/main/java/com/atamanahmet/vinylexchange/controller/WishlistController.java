package com.atamanahmet.vinylexchange.controller;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.service.WishlistService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistBulkRequest;
import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistRequest;
import com.atamanahmet.vinylexchange.dto.listing.WishlistItemDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlists")
public class WishlistController {

        private final WishlistService wishlistService;

        @GetMapping
        public ResponseEntity<List<WishlistItemDTO>> getMyWishlist(){

                List<WishlistItemDTO> wishlistDTO = wishlistService.getWishlistByUserId(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(wishlistDTO);
        }

        @PostMapping
        public ResponseEntity<List<WishlistItemDTO>> addToWishlist(
                        @RequestBody AddToWishlistRequest request) {

                wishlistService.addToWishlist(UserUtil.getCurrentUserId(), request);

                List<WishlistItemDTO> wishlistDTO = wishlistService.getWishlistByUserId(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(wishlistDTO);

        }

        @PostMapping("/bulk")
        public ResponseEntity<List<WishlistItemDTO>> addToWishlistBulk(
                        @RequestBody AddToWishlistBulkRequest bulkRequest) {

                System.out.println(bulkRequest);

                wishlistService.addAllToWishlist(UserUtil.getCurrentUserId(), bulkRequest);

                List<WishlistItemDTO> wishlistDTO = wishlistService.getWishlistByUserId(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(wishlistDTO);

        }

        @DeleteMapping("/{wishlistItemId}")
        public ResponseEntity<List<WishlistItemDTO>> removeFromWishlist(
                        @PathVariable(name = "wishlistItemId") UUID wishlistItemId) {

                wishlistService.removeFromWishlist(UserUtil.getCurrentUserId(), wishlistItemId);

                List<WishlistItemDTO> wishlistDTO = wishlistService.getWishlistByUserId(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(wishlistDTO);
        }
}
