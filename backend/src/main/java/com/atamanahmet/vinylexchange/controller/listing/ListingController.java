package com.atamanahmet.vinylexchange.controller.listing;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.dto.*;
import com.atamanahmet.vinylexchange.dto.listing.*;
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
        public ResponseEntity<?> getPublicListings(
                        @PageableDefault(size = 50) Pageable pageable) {

                Page<ListingDTO> listingDTOs = listingService.getPublicListings(pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(listingDTOs);
        }

        @GetMapping("/username")
        public ResponseEntity<?> getPublicListingsByUser(
                        @PathVariable(name = "username", required = true) String username,
                        @PageableDefault(size = 50) Pageable pageable) {

                Page<ListingDTO> listingDTOs = listingService.getAllAvailableListingsByUser(username, pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(listingDTOs);
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

                Set<UUID> cartListingIds = cartService.getCartDTO(userId)
                        .getItems()
                        .stream()
                        .map(CartItemDTO::getListingId)
                        .collect(Collectors.toSet());

                List<ListingDTO> promoted = listingService.getPromotedListingDTOs(cartListingIds);

                return ResponseEntity.status(HttpStatus.OK).body(promoted);
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> createListing(
                        @RequestPart("listing") @Valid CreateListingRequest request,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {

                User user = UserUtil.getCurrentUser();

                listingService.createNewListing(
                                request,
                                images,
                                user);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .build();
        }

        @PatchMapping("/{listingId}")
        public ResponseEntity<?> updateListing(
                        @PathVariable UUID listingId,
                        @RequestPart("listing") UpdateListingRequest request,
                        @RequestPart(value = "images", required = false) List<MultipartFile> newImages) {

                UserUtil.isAuthenticated();

               ListingDTO updatedListing = listingService.updateListing(listingId, request, newImages, UserUtil.getCurrentUserId());

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(updatedListing);
        }

        @GetMapping("/{listingId}")
        public ResponseEntity<?> getListing(@PathVariable(name = "listingId", required = true) UUID listingId) {

                ListingDTO listingDTO = listingService.getListingDTOById(listingId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(listingDTO);

        }

        @DeleteMapping("/{listingId}")
        public ResponseEntity<?> deleteListing(
                        @PathVariable(name = "listingId", required = true) UUID listingId) {

                UserUtil.isAuthenticated();

                listingService.deleteListing(listingId);

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
        @PatchMapping("/promote/{listingId}")
        public ResponseEntity<?> promoteListing(
                        @PathVariable(name = "listingId", required = true) UUID listingId,
                        @RequestBody PromoteRequest promoteRequest) {

                UserUtil.isAuthenticated();

                listingService.promoteListing(listingId, promoteRequest.action(), UserUtil.getCurrentUserDetails());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .build();
        }

        // admin
        @PreAuthorize("hasRole('ADMIN')")
        @PatchMapping("/freeze/{listingId}")
        public ResponseEntity<?> freezeListing(
                        @PathVariable(name = "listingId", required = true) UUID listingId,
                        @RequestBody FreezeRequest freezeRequest) {

                listingService.freezeListing(listingId, freezeRequest.action(), UserUtil.getCurrentUserDetails());

                if (freezeRequest.action()) {

                        listingService.promoteListing(listingId, false, UserUtil.getCurrentUserDetails());
                }

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .build();
        }
}
