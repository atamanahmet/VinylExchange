package com.atamanahmet.vinylexchange.service.listing;

import com.atamanahmet.vinylexchange.common.money.ListingPriceCalculator;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.dto.listing.CreateListingRequest;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.dto.listing.ListingFilterCriteria;
import com.atamanahmet.vinylexchange.dto.listing.ListingPriceHistoryDto;
import com.atamanahmet.vinylexchange.dto.listing.ListingSummaryDto;
import com.atamanahmet.vinylexchange.dto.listing.ListingSummaryResponse;
import com.atamanahmet.vinylexchange.dto.listing.UpdateListingRequest;
import com.atamanahmet.vinylexchange.event.ListingCreatedEvent;
import com.atamanahmet.vinylexchange.event.ListingUpdatedEvent;
import com.atamanahmet.vinylexchange.exception.InsufficientStockException;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.exception.ListingCreationException;
import com.atamanahmet.vinylexchange.exception.ListingNotFoundException;
import com.atamanahmet.vinylexchange.exception.RegistrationValidationException;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.atamanahmet.vinylexchange.mapper.ListingMapper;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import com.atamanahmet.vinylexchange.repository.listing.ListingSpecifications;
import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;
import com.atamanahmet.vinylexchange.service.media.CoverArtService;
import com.atamanahmet.vinylexchange.service.media.FileStorageService;
import com.atamanahmet.vinylexchange.service.media.ImageStorageRouter;
import com.atamanahmet.vinylexchange.service.order.OrderAccessService;
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final ListingCacheStore listingCacheStore;
    private final ListingPriceHistoryService listingPriceHistoryService;
    private final OrderAccessService orderAccessService;
    private final ImageStorageRouter imageStorageRouter;
    private final FileStorageService fileStorageService;
    private final CoverArtService coverArtService;
    private final ApplicationEventPublisher eventPublisher;
    private final ListingPriceCalculator priceCalculator;
    private final UserAddressService userAddressService;

    public Page<ListingDTO> getPublicListings(Pageable pageable) {
        return listingRepository.findAllWithStatus(ListingStatus.AVAILABLE, pageable)
                .map(listingMapper::toDTO);
    }

    public Page<ListingSummaryResponse> getPublicListingsSummary(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int from = page * size;
        int to = from + size;

        List<ListingSummaryDto> cached = listingCacheStore.getTop60ForSort(pageable);

        if (from >= cached.size()) {
            // request is beyond cache, hit DB directly
            return listingRepository.findAllWithStatus(ListingStatus.AVAILABLE, pageable)
                    .map(listingMapper::toSummaryDto)
                    .map(listingMapper::toResponse);
        }

        List<ListingSummaryResponse> slice = cached.subList(from, Math.min(to, cached.size()))
                .stream()
                .map(listingMapper::toResponse)
                .toList();

        return new PageImpl<>(slice, pageable, cached.size());
    }

    @Transactional(readOnly = true)
    public Page<ListingSummaryResponse> search(ListingFilterCriteria criteria, Pageable pageable) {
        if (criteria.isEmpty()) {
            return getPublicListingsSummary(pageable);
        }

        Specification<Listing> spec = ListingSpecifications.isPubliclyAvailable()
                .and(ListingSpecifications.hasCountry(criteria.country()))
                .and(ListingSpecifications.hasFormat(criteria.format()))
                .and(ListingSpecifications.hasSpeedRpm(criteria.speedRpm()))
                .and(ListingSpecifications.hasVinylSubtype(criteria.vinylSubtype()))
                .and(ListingSpecifications.hasCondition(criteria.condition()))
                .and(ListingSpecifications.hasYearFrom(criteria.yearFrom()))
                .and(ListingSpecifications.hasYearTo(criteria.yearTo()))
                .and(ListingSpecifications.hasGenreIds(criteria.genreIds()))
                .and(ListingSpecifications.hasTradeable(criteria.tradeable()))
                .and(ListingSpecifications.hasPriceFrom(criteria.priceFromKurus()))
                .and(ListingSpecifications.hasPriceTo(criteria.priceToKurus()))
                .and(ListingSpecifications.hasOwnerUsername(criteria.ownerUsername()));

        return listingRepository.findAll(spec, pageable)
                .map(listingMapper::toSummaryDto)
                .map(listingMapper::toResponse);
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

    @Transactional(readOnly = true)
    public ListingDTO getListingByPublicId(String publicId) {
        Listing listing = listingRepository.findByPublicId(publicId)
                .orElseThrow(ListingNotFoundException::new);
        Integer discountPercent = priceCalculator.discountPercent(
                listing.getOriginalPriceKurus(),
                listing.getPriceKurus(),
                listing.getPriceLastChangedAt()).orElse(null);
        return listingMapper.toDTOWithImages(listing, discountPercent);
    }

    @Transactional(readOnly = true)
    public List<ListingPriceHistoryDto> getPriceHistoryForPublicId(String publicId) {
        Listing listing = listingRepository.findByPublicId(publicId)
                .orElseThrow(ListingNotFoundException::new);

        return listingPriceHistoryService.getHistoryForListing(listing.getId())
                .stream()
                .map(ListingPriceHistoryDto::from)
                .toList();
    }

    /**
     * Returns promoted listings excluding ones already in cart
     */
    public List<ListingDTO> getPromotedListingDTOs(Set<String> excludePublicIds) {
        return listingRepository.findByPromoteTrue()
                .stream()
                .filter(listing -> !excludePublicIds.contains(listing.getPublicId()))
                .limit(5)
                .map(listingMapper::toDTO)
                .toList();
    }

    @CacheEvict(value = "listings", allEntries = true)
    @Transactional
    public ListingDTO createNewListing(
            CreateListingRequest request,
            List<MultipartFile> images,
            com.atamanahmet.vinylexchange.domain.entity.User owner) {

        if (!userAddressService.hasShippingAddress(owner.getId())) {
            throw new RegistrationValidationException(
                    "You must add a shipping address before creating a listing");
        }

        try {
            Listing listing = listingMapper.toEntity(request);
            listing.setOwner(owner);

            Listing savedListing = listingRepository.save(listing);
            listingPriceHistoryService.recordCreation(savedListing, owner.getId().toString());

            if (images != null && !images.isEmpty()) {
                List<ImageUploadResult> results = imageStorageRouter.upload(
                        toImageSources(images), savedListing.getId());
                attachImages(savedListing, results);
                savedListing = listingRepository.save(savedListing);
            } else if (request.getMbId() != null) {
                try {
                    attachPlaceholderCover(savedListing, request.getMbId());
                    savedListing = listingRepository.save(savedListing);
                } catch (Exception placeholderError) {
                    log.warn(
                            "Placeholder cover failed for listing {} (mbId {}): {}",
                            savedListing.getId(),
                            request.getMbId(),
                            placeholderError.getMessage());
                }
            }

            eventPublisher.publishEvent(
                    ListingCreatedEvent.builder().listing(savedListing).build());

            Integer discountPercent = priceCalculator.discountPercent(
                    savedListing.getOriginalPriceKurus(),
                    savedListing.getPriceKurus(),
                    savedListing.getPriceLastChangedAt()).orElse(null);
            return listingMapper.toDTOWithImages(savedListing, discountPercent);

        } catch (Exception e) {
            log.error("Listing creation failed for user {}: {}", owner.getUsername(), e.getMessage());
            throw new ListingCreationException("Listing creation failed for user: ", owner.getUsername());
        }
    }

    @CacheEvict(value = "listings", allEntries = true)
    @Transactional
    public ListingDTO updateListing(
            String publicId,
            UpdateListingRequest request,
            List<MultipartFile> newImages,
            UUID userId) {
        Listing listing = listingRepository.findByPublicId(publicId)
                .orElseThrow(ListingNotFoundException::new);
        return updateListing(listing.getId(), request, newImages, userId);
    }

    @CacheEvict(value = "listings", allEntries = true)
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

    @CacheEvict(value = "listings", allEntries = true)
    public void deleteListing(String publicId) {
        Listing listing = listingRepository.findByPublicId(publicId)
                .orElseThrow(ListingNotFoundException::new);
        deleteListing(listing.getId());
    }

    @CacheEvict(value = "listings", allEntries = true)
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

        boolean hasNewImages = newImages != null && !newImages.isEmpty();
        if (keptUrls == null && !hasNewImages) {
            return;
        }

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
     * When seller uploads no photos, attach cached cover-art-archive image under
     * /uploads/placeholders/{mbId}/ so the UI can show the placeholder badge.
     */
    private void attachPlaceholderCover(Listing listing, UUID mbId) throws IOException {
        List<String> placeholderPaths = fileStorageService.getPlaceholderImagePaths(mbId);

        if (placeholderPaths.isEmpty()) {
            String coverUrl = coverArtService.fetchCoverUrl(mbId);
            if (coverUrl == null) {
                return;
            }

            ImageSource imageSource = fileStorageService.downloadExternalImage(coverUrl);
            if (imageSource == null) {
                return;
            }

            fileStorageService.savePlaceholderImage(imageSource, mbId);
            placeholderPaths = fileStorageService.getPlaceholderImagePaths(mbId);
        }

        if (placeholderPaths.isEmpty()) {
            return;
        }

        int position = 0;
        for (String url : placeholderPaths) {
            listing.getImages().add(ListingImage.builder()
                    .publicId(url)
                    .secureUrl(url)
                    .position(position++)
                    .provider(StorageProvider.LOCAL)
                    .uploadedAt(LocalDateTime.now())
                    .listing(listing)
                    .build());
        }

        listing.setMainImageUrl(placeholderPaths.get(0));
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

    public Listing findListingByPublicId(String publicId) {
        return listingRepository.findByPublicId(publicId)
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

    @CacheEvict(value = "listings", allEntries = true)
    public void promoteListing(String publicId, Boolean action, UserDetailsImpl currentUser) {
        Listing listing = listingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found to promote"));
        promoteListing(listing.getId(), action, currentUser);
    }

    @CacheEvict(value = "listings", allEntries = true)
    public void promoteListing(UUID listingId, Boolean action, UserDetailsImpl currentUser) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found to promote"));
        listing.setPromote(action);
        listing.setPromotedBy(currentUser.getUsername());
        listing.setPromotedById(currentUser.getId());
        listing.setPromotedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    @CacheEvict(value = "listings", allEntries = true)
    public void freezeListing(String publicId, Boolean action, UserDetailsImpl currentUser) {
        Listing listing = listingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found to freeze"));
        freezeListing(listing.getId(), action, currentUser);
    }

    @CacheEvict(value = "listings", allEntries = true)
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