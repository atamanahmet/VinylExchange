package com.atamanahmet.vinylexchange.service.listing;

import com.atamanahmet.vinylexchange.common.money.ListingPriceCalculator;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.dto.listing.CreateListingRequest;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.dto.listing.UpdateListingRequest;
import com.atamanahmet.vinylexchange.event.ListingCreatedEvent;
import com.atamanahmet.vinylexchange.event.ListingUpdatedEvent;
import com.atamanahmet.vinylexchange.exception.InsufficientStockException;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.ListingCreationException;
import com.atamanahmet.vinylexchange.exception.ListingNotFoundException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.atamanahmet.vinylexchange.mapper.ListingMapper;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;
import com.atamanahmet.vinylexchange.service.media.CoverArtService;
import com.atamanahmet.vinylexchange.service.media.ImageStorageRouter;
import com.atamanahmet.vinylexchange.service.order.OrderAccessService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;
    private final ListingPriceHistoryService listingPriceHistoryService;
    private final OrderAccessService orderAccessService;
    private final ImageStorageRouter imageStorageRouter;
    private final CoverArtService coverArtService;
    private final ApplicationEventPublisher eventPublisher;
    private final ListingPriceCalculator priceCalculator;

    public Page<ListingDTO> getPublicListings(Pageable pageable) {
        return listingRepository.findAllWithStatus(ListingStatus.AVAILABLE, pageable)
                .map(listingMapper::toDTO);
    }

    public Page<ListingDTO> getAllAvailableListingsByUser(String username, Pageable pageable) {
        return listingRepository.findAllWithStatusAndUsername(ListingStatus.AVAILABLE, username, pageable)
                .map(listingMapper::toDTO);
    }

    /**
     * Detail page, full images via join fetch
     */
    public ListingDTO getListingDTOById(UUID listingId) {
        Listing listing = listingRepository.findByIdWithImages(listingId)
                .orElseThrow(ListingNotFoundException::new);
        Integer discountPercent = priceCalculator.discountPercent(
                listing.getOriginalPriceKurus(),
                listing.getPriceKurus(),
                listing.getPriceLastChangedAt()).orElse(null);
        return listingMapper.toDTOWithImages(listing, discountPercent);
    }

    /**
     * Returns promoted listings excluding ones already in cart
     */
    public List<ListingDTO> getPromotedListingDTOs(Set<UUID> excludeListingIds) {
        return listingRepository.findByPromoteTrue()
                .stream()
                .filter(listing -> !excludeListingIds.contains(listing.getId()))
                .limit(5)
                .map(listingMapper::toDTO)
                .toList();
    }

    @Transactional
    public void createNewListing(
            CreateListingRequest request,
            List<MultipartFile> images,
            com.atamanahmet.vinylexchange.domain.entity.User owner) {

        try {
            Listing listing = listingMapper.toEntity(request);
            listing.setOwner(owner);

            Listing savedListing = listingRepository.save(listing);
            listingPriceHistoryService.recordCreation(savedListing, owner.getId().toString());

            if (images != null && !images.isEmpty()) {
                List<ImageUploadResult> results = imageStorageRouter.upload(
                        toImageSources(images), savedListing.getId());
                attachImages(savedListing, results);
                listingRepository.save(savedListing);
            }

            eventPublisher.publishEvent(
                    ListingCreatedEvent.builder().listing(savedListing).build());

        } catch (Exception e) {
            log.error("Listing creation failed for user {}: {}", owner.getUsername(), e.getMessage());
            throw new ListingCreationException("Listing creation failed for user: ", owner.getUsername());
        }
    }

    @Transactional
    public ListingDTO updateListing(
            UUID listingId,
            UpdateListingRequest request,
            List<MultipartFile> newImages,
            UUID userId) {

        Listing existingListing = listingRepository.findByIdWithImages(listingId)
                .orElseThrow(ListingNotFoundException::new);

        if (!existingListing.getOwner().getId().equals(userId)) {
            throw new UnauthorizedActionException("Listing update unauthorized for userId: " + userId);
        }

        boolean isPriceUpdate = request.getPriceKurus() != null || request.getSellerEarningsKurus() != null;

        if (isPriceUpdate && orderAccessService.hasActiveOrderForListing(listingId)) {
            throw new InvalidOrderOperationException(
                    "Cannot update listing price while an active order exists for listingId: " + listingId);
        }

        try {
            handleImageUpdates(existingListing, request.getImagePaths(), newImages);

            long oldPrice = existingListing.getPriceKurus();
            long oldSellerEarnings = existingListing.getSellerEarningsKurus();
            long oldPlatformCut = existingListing.getPlatformCutKurus();

            listingMapper.updateEntityFromRequest(existingListing, request);
            if (isPriceUpdate) {
                existingListing.setPriceLastChangedAt(LocalDateTime.now());
            }
            Listing savedListing = listingRepository.save(existingListing);

            if (isPriceUpdate) {
                listingPriceHistoryService.recordUpdate(
                        savedListing, oldPrice, oldSellerEarnings, oldPlatformCut, userId.toString());
            }

            eventPublisher.publishEvent(
                    ListingUpdatedEvent.builder().listing(savedListing).build());

            Integer discountPercent = priceCalculator.discountPercent(
                    savedListing.getOriginalPriceKurus(),
                    savedListing.getPriceKurus(),
                    savedListing.getPriceLastChangedAt()).orElse(null);
            return listingMapper.toDTOWithImages(savedListing, discountPercent);

        } catch (Exception e) {
            log.error("Error updating listing {}: {}", listingId, e.getMessage());
            throw new ListingCreationException("Listing update failed for listingId: ", listingId.toString());
        }
    }

    public void deleteListing(UUID listingId) {
        UUID userId = UserUtil.getCurrentUserId();

        Listing existingListing = listingRepository.findByIdWithImages(listingId)
                .orElseThrow(ListingNotFoundException::new);

        if (!existingListing.getOwner().getId().equals(userId)) {
            log.warn("Unauthorized delete attempt by userId: {}", userId);
            throw new UnauthorizedActionException("Users can only delete their own listings.");
        }

        imageStorageRouter.deleteAll(existingListing.getImages());
        listingRepository.delete(existingListing);
    }

    /**
     * Deletes removed images from storage, uploads new ones
     * Removes images not in keptUrls, keeps mainImageUrl in sync
     */
    private void handleImageUpdates(
            Listing listing,
            List<String> keptUrls,
            List<MultipartFile> newImages) throws IOException {

        List<ListingImage> toDelete = listing.getImages().stream()
                .filter(img -> keptUrls == null || !keptUrls.contains(img.getSecureUrl()))
                .toList();

        toDelete.forEach(img -> {
            imageStorageRouter.delete(img);
            listing.getImages().remove(img);
        });

        if (newImages != null && !newImages.isEmpty()) {
            int nextPosition = listing.getImages().size();
            List<ImageUploadResult> results = imageStorageRouter.upload(
                    toImageSources(newImages), listing.getId());

            for (ImageUploadResult result : results) {
                listing.getImages().add(ListingImage.builder()
                        .publicId(result.getPublicId())
                        .secureUrl(result.getSecureUrl())
                        .position(nextPosition++)
                        .provider(result.getProvider())
                        .uploadedAt(LocalDateTime.now())
                        .listing(listing)
                        .build());
            }
        }

        listing.setMainImageUrl(listing.getImages().isEmpty()
                ? null
                : listing.getImages().get(0).getSecureUrl());
    }

    /**
     * Attaches uploaded results to listing, sets mainImageUrl from first image
     */
    private void attachImages(Listing listing, List<ImageUploadResult> results) {
        for (ImageUploadResult result : results) {
            listing.getImages().add(ListingImage.builder()
                    .publicId(result.getPublicId())
                    .secureUrl(result.getSecureUrl())
                    .position(result.getPosition())
                    .provider(result.getProvider())
                    .uploadedAt(LocalDateTime.now())
                    .listing(listing)
                    .build());
        }

        if (!listing.getImages().isEmpty()) {
            listing.setMainImageUrl(listing.getImages().get(0).getSecureUrl());
        }
    }

    private List<ImageSource> toImageSources(List<MultipartFile> files) {
        return files.stream().map(file -> {
            try {
                return new ImageSource(
                        file.getInputStream(),
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read uploaded file: " + e.getMessage());
            }
        }).toList();
    }

    public Listing findListingById(UUID listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(ListingNotFoundException::new);
    }

    public List<Listing> getListingsByIdsWithLock(List<UUID> listingIds) {
        return listingRepository.findByIdInWithLock(listingIds);
    }

    public List<Listing> getListingsByIds(List<UUID> listingIds) {
        return listingRepository.findAllByIdIn(listingIds);
    }

    public List<ListingDTO> getListingDTOsWithIds(List<UUID> listingIdList) {
        return listingRepository.findAllByIdIn(listingIdList)
                .stream().map(listingMapper::toDTO).toList();
    }

    public List<ListingDTO> getUserListingsWithStatus(UUID ownerId, ListingStatus status) {
        return listingRepository.findAllByOwner_IdAndStatus(ownerId, status)
                .stream().map(listingMapper::toDTO).toList();
    }

    public void saveAllListing(List<Listing> listingList) {
        listingRepository.saveAll(listingList);
    }

    public Listing saveListing(Listing listing) {
        return listingRepository.save(listing);
    }

    public long totalCount() {
        return listingRepository.count();
    }

    public boolean isExistByTitle(String title) {
        return listingRepository.existsByTitle(title);
    }

    public boolean isAvailableForTrade(UUID listingId) {
        return listingRepository.isAvailableForTrade(listingId, ListingStatus.AVAILABLE);
    }

    public String getOwnerUsernameByListingId(UUID listingId) {
        return findListingById(listingId).getOwner().getUsername();
    }

    public void decreaseItemQuantity(UUID listingId, int quantity) {
        Listing listing = findListingById(listingId);
        if (!listing.hasEnoughStock(quantity)) throw new InsufficientStockException();
        listing.setStockQuantity(listing.getStockQuantity() - quantity);
        listingRepository.save(listing);
    }

    public void restoreStock(UUID listingId, int quantity) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found for restock"));
        listing.setStockQuantity(listing.getStockQuantity() + quantity);
        listingRepository.save(listing);
    }

    public void promoteListing(UUID listingId, Boolean action, UserDetailsImpl currentUser) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found to promote"));
        listing.setPromote(action);
        listing.setPromotedBy(currentUser.getUsername());
        listing.setPromotedById(currentUser.getId());
        listing.setPromotedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    public void freezeListing(UUID listingId, Boolean action, UserDetailsImpl currentUser) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found to freeze"));
        listing.setOnHold(action);
        listingRepository.save(listing);
    }

    public List<Listing> getPromotedListings() {
        return listingRepository.findByPromoteTrue();
    }

    public List<ListingDTO> getAllListingDTOs() {
        return listingRepository.findAll().stream().map(listingMapper::toDTO).toList();
    }

    public Page<Listing> getAllListingsPageable(Pageable pageable) {
        return listingRepository.findAll(pageable);
    }
}