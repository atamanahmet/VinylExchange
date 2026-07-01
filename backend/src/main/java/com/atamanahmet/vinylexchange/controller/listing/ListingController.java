package com.atamanahmet.vinylexchange.controller.listing;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.dto.*;
import com.atamanahmet.vinylexchange.dto.listing.*;
import com.atamanahmet.vinylexchange.domain.enums.Country;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;
import com.atamanahmet.vinylexchange.dto.order.CartItemDTO;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.service.order.CartService;
import com.atamanahmet.vinylexchange.service.listing.PricePreviewService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import com.atamanahmet.vinylexchange.domain.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

        private final ListingService listingService;
        private final PricePreviewService pricePreviewService;
        private final CartService cartService;

        @GetMapping
        public ResponseEntity<Page<ListingSummaryDto>> getPublicListings(
                        @RequestParam(required = false) List<String> country,
                        @RequestParam(required = false) List<MediaFormat> format,
                        @RequestParam(required = false) List<Integer> speedRpm,
                        @RequestParam(required = false) List<VinylSubtype> vinylSubtype,
                        @RequestParam(required = false) List<String> condition,
                        @RequestParam(required = false) Integer yearFrom,
                        @RequestParam(required = false) Integer yearTo,
                        @RequestParam(required = false) List<Long> genreIds,
                        @RequestParam(required = false) Boolean tradeable,
                        @RequestParam(required = false) Long priceFromKurus,
                        @RequestParam(required = false) Long priceToKurus,
                        @RequestParam(required = false) String ownerUsername,
                        @PageableDefault(size = 50) Pageable pageable) {

                List<Country> resolvedCountries = country == null ? null : country.stream()
                                .filter(value -> value != null && !value.isBlank())
                                .map(Country::fromIsoCode)
                                .filter(Objects::nonNull)
                                .toList();

                if (resolvedCountries != null && resolvedCountries.isEmpty()) {
                        resolvedCountries = null;
                }

                ListingFilterCriteria criteria = new ListingFilterCriteria(
                                resolvedCountries,
                                format,
                                speedRpm,
                                vinylSubtype,
                                condition,
                                yearFrom,
                                yearTo,
                                genreIds,
                                tradeable,
                                priceFromKurus,
                                priceToKurus,
                                ownerUsername);

                Page<ListingSummaryDto> listings = listingService.search(criteria, pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(listings);
        }

        @GetMapping("/by-username/{username}")
        public ResponseEntity<Page<ListingSummaryDto>> getPublicListingsByUsername(
                        @PathVariable String username,
                        @RequestParam(required = false) List<String> country,
                        @RequestParam(required = false) List<MediaFormat> format,
                        @RequestParam(required = false) List<Integer> speedRpm,
                        @RequestParam(required = false) List<VinylSubtype> vinylSubtype,
                        @RequestParam(required = false) List<String> condition,
                        @RequestParam(required = false) Integer yearFrom,
                        @RequestParam(required = false) Integer yearTo,
                        @RequestParam(required = false) List<Long> genreIds,
                        @RequestParam(required = false) Boolean tradeable,
                        @RequestParam(required = false) Long priceFromKurus,
                        @RequestParam(required = false) Long priceToKurus,
                        @PageableDefault(size = 50) Pageable pageable) {

                List<Country> resolvedCountries = country == null ? null : country.stream()
                                .filter(value -> value != null && !value.isBlank())
                                .map(Country::fromIsoCode)
                                .filter(Objects::nonNull)
                                .toList();

                if (resolvedCountries != null && resolvedCountries.isEmpty()) {
                        resolvedCountries = null;
                }

                ListingFilterCriteria criteria = new ListingFilterCriteria(
                                resolvedCountries,
                                format,
                                speedRpm,
                                vinylSubtype,
                                condition,
                                yearFrom,
                                yearTo,
                                genreIds,
                                tradeable,
                                priceFromKurus,
                                priceToKurus,
                                username);

                Page<ListingSummaryDto> listings = listingService.search(criteria, pageable);

                return ResponseEntity.ok(listings);
        }

        // for admin actions only, promote, freeze, remove etc
        // TODO: separate admin controller wip
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/all")
        public ResponseEntity<?> getAllListings() {

                List<ListingDTO> listingDTOs = listingService.getAllListingDTOs();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(listingDTOs);
        }

        @GetMapping("/promote")
        public ResponseEntity<?> getPromotedListingsForUser() {

                UUID userId = UserUtil.getCurrentUserId();

                Set<String> cartListingPublicIds = cartService.getCartDTO(userId)
                        .getItems()
                        .stream()
                        .map(CartItemDTO::getPublicId)
                        .collect(Collectors.toSet());

                List<ListingDTO> promoted = listingService.getPromotedListingDTOs(cartListingPublicIds);

                return ResponseEntity.status(HttpStatus.OK).body(promoted);
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> createListing(
                        @RequestPart("listing") @Valid CreateListingRequest request,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {

                User user = UserUtil.getCurrentUser();

                ListingDTO createdListing = listingService.createNewListing(
                                request,
                                images,
                                user);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(createdListing);
        }

        @PatchMapping("/{publicId}")
        public ResponseEntity<?> updateListing(
                        @PathVariable String publicId,
                        @RequestPart("listing") UpdateListingRequest request,
                        @RequestPart(value = "images", required = false) List<MultipartFile> newImages) {

                UserUtil.isAuthenticated();

               ListingDTO updatedListing = listingService.updateListing(publicId, request, newImages, UserUtil.getCurrentUserId());

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(updatedListing);
        }

        @GetMapping("/{publicId}")
        public ResponseEntity<?> getListing(@PathVariable String publicId) {
                return ResponseEntity.ok(listingService.getListingByPublicId(publicId));
        }

        @DeleteMapping("/{publicId}")
        public ResponseEntity<?> deleteListing(
                        @PathVariable(name = "publicId", required = true) String publicId) {

                UserUtil.isAuthenticated();

                listingService.deleteListing(publicId);

                return ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .build();
        }

        @PostMapping("/price/preview")
        public ResponseEntity<ListingPriceResultDTO> previewPrice(
                @RequestBody @Valid PricePreviewRequest request) {

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(pricePreviewService.preview(request));
        }

        // admin
        @PreAuthorize("hasRole('ADMIN')")
        @PatchMapping("/promote/{publicId}")
        public ResponseEntity<?> promoteListing(
                        @PathVariable(name = "publicId", required = true) String publicId,
                        @RequestBody PromoteRequest promoteRequest) {

                UserUtil.isAuthenticated();

                listingService.promoteListing(publicId, promoteRequest.action(), UserUtil.getCurrentUserDetails());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .build();
        }

        // admin
        @PreAuthorize("hasRole('ADMIN')")
        @PatchMapping("/freeze/{publicId}")
        public ResponseEntity<?> freezeListing(
                        @PathVariable(name = "publicId", required = true) String publicId,
                        @RequestBody FreezeRequest freezeRequest) {

                listingService.freezeListing(publicId, freezeRequest.action(), UserUtil.getCurrentUserDetails());

                if (freezeRequest.action()) {

                        listingService.promoteListing(publicId, false, UserUtil.getCurrentUserDetails());
                }

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .build();
        }
}
