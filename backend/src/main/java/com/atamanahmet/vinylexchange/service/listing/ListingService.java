package com.atamanahmet.vinylexchange.service.listing;

import java.io.IOException;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.dto.listing.CreateListingRequest;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.dto.listing.UpdateListingRequest;
import com.atamanahmet.vinylexchange.dto.order.CartItemDTO;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceDTO;
import com.atamanahmet.vinylexchange.exception.*;
import com.atamanahmet.vinylexchange.mapper.ListingMapper;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import com.atamanahmet.vinylexchange.service.order.CartService;
import com.atamanahmet.vinylexchange.service.media.CoverArtService;
import com.atamanahmet.vinylexchange.service.media.FileStorageService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.event.ListingCreatedEvent;
import com.atamanahmet.vinylexchange.event.ListingUpdatedEvent;

import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;

import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.domain.entity.TradePreference;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ListingService {

    private final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final FileStorageService fileStorageService;
    private final CartService cartService;
    private final CoverArtService coverArtService;

    // publish event to wishlist check -> send notification
    private final ApplicationEventPublisher eventPublisher;

    private final ListingMapper listingMapper;

    public ListingService(
            ListingRepository listingRepository,
            FileStorageService fileStorageService,
            @Lazy CartService cartService,
            WebClient webClient,
            CoverArtService coverArtService,
            ApplicationEventPublisher eventPublisher,
            ListingMapper listingMapper) {

        this.listingRepository = listingRepository;
        this.fileStorageService = fileStorageService;
        this.cartService = cartService;
        this.coverArtService = coverArtService;
        this.eventPublisher = eventPublisher;
        this.listingMapper = listingMapper;
    }

    public List<Listing> getAllListings() {

        return listingRepository.findAll();
    }

    public Page<Listing> getAllListingsPageable(Pageable pageable) {

        return listingRepository.findAll(pageable);
    }

    public Page<ListingDTO> getAllAvailableListings(Pageable pageable) {

        Page<Listing> listingsPage = listingRepository.findAllWithStatus(ListingStatus.AVAILABLE, pageable);

        return listingsPage.map(this::toDTO);
    }

    public Page<ListingDTO> getAllAvailableListingsByUser(String username, Pageable pageable) {

        Page<Listing> listingsPage = listingRepository.findAllWithStatusAndUsername(ListingStatus.AVAILABLE, username, pageable);

        return listingsPage.map(this::toDTO);
    }

    public Page<ListingDTO> getPublicListings(Pageable pageable) {

        Page<Listing> listingsPage = listingRepository.findAllWithStatus(ListingStatus.AVAILABLE, pageable);

        return listingsPage.map(this::toDTO);
    }

    public List<Listing> getPromotedListings() {

        return listingRepository.findByPromoteTrue();
    }

    public List<ListingDTO> getFilteredPromotedListingDTOs(UUID userId) {

        Set<UUID> cartListingIds = cartService
                .getCartDTO(userId)
                .getItems()
                .stream()
                .map(CartItemDTO::getListingId).collect(Collectors.toSet());

        if (cartListingIds.isEmpty()) {
            return List.of();
        }

        List<Listing> promotedListings = listingRepository.findByPromoteTrue();

        // only first 3 item to fit in cartPage
        List<Listing> filteredPromotedList = promotedListings.stream()
                .filter(listing -> !cartListingIds.contains(listing.getId()))
                .limit(5)
                .toList();

        return filteredPromotedList.stream()
                .map(this::toDTO)
                .toList();
    }

    public void saveAllListing(List<Listing> listingList) {
        listingRepository.saveAll(listingList);
    }

    public List<Listing> getListingsByIds(List<UUID> listingIds) {

        return listingRepository.findAllByIdIn(listingIds);
    }

    public List<Listing> getListingsByIdsWithLock(List<UUID> listingIds) {

        return listingRepository.findAllByIdIn(listingIds);
    }

    public List<ListingDTO> getListingDTOsWithIds(List<UUID> listingIdList) {

        List<Listing> resultListings = listingRepository.findAllByIdIn(listingIdList);

        return toDTOList(resultListings);
    }

    // Admin only, maybe unnecessary
    // TODO: check again
    public List<ListingDTO> getAllListingsDTOs() {

        List<Listing> allListings = listingRepository.findAll();

        return toDTOList(allListings);
    }

    public Listing saveListing(Listing listing) {

        return listingRepository.save(listing);
    }

    public void deleteListing(UUID listingId) {

        UUID userId = UserUtil.getCurrentUserId();

        Listing existingListing = listingRepository.findById(listingId).orElseThrow(ListingNotFoundException::new);


        if (!existingListing.getOwner().getId().equals(userId)) {

            logger.warn("Listing delete unauthorized, action caused by userId: " + userId);

            throw new UnauthorizedActionException("Users only delete their own listings.");
        }

        listingRepository.delete(existingListing);

        try{
            fileStorageService.deleteListingImages(listingId);

        } catch (Exception e) {
            logger.error("Listing files deletion failed for id: {} ", listingId);
            throw new RuntimeException(e);
        }

    }

    public Listing findListingById(UUID listingId) {

        return listingRepository
                .findById(listingId)
                .orElseThrow(ListingNotFoundException::new);
    }

    public long totalCount() {
        return listingRepository.count();
    }

    // TODO: refactor
    @Transactional
    public void createNewListing(
            CreateListingRequest request,
            List<MultipartFile> images,
            User owner) {

        try {

            Listing listing = listingMapper.toEntity(request);

            listing.setOwner(owner);


            Listing savedListing = listingRepository.save(listing);

            if (images != null && !images.isEmpty()) {
                handleImageUpload(savedListing.getId(), images);
            }

            if (listing.getMbId() != null) {
                coverArtService.fetchAndSaveCoverAsync(
                        savedListing.getId(),
                        listing.getMbId());
            }

            eventPublisher.publishEvent(
                    ListingCreatedEvent.builder()
                            .listing(savedListing).build());

        } catch (Exception e) {
            throw new ListingCreationException("Listing creation failed for user: ", owner.getUsername());
        }
    }

    private void handleImageUpload(UUID listingId, List<MultipartFile> images) {
        try {
            List<ImageSource> imageSources = images.stream()
                    .map(this::convertToImageSource)
                    .toList();

            fileStorageService.saveImages(imageSources, listingId);

        } catch (IOException e) {
            logger.error("Failed to upload images for listing {}", listingId, e);
            throw new ImageUploadException("Failed to upload images");
        }
    }

    // for exception catch, constructor wont catch
    public ImageSource convertToImageSource(MultipartFile image) {
        try {
            return new ImageSource(
                    image.getInputStream(),
                    image.getOriginalFilename(),
                    image.getContentType(),
                    image.getSize());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read uploaded image to ImageSource: " + e.getMessage());
        }
    }

    public List<ImageSource> getImageSourceList(List<MultipartFile> images) {
        List<ImageSource> imageSources = new ArrayList<>();

        for (MultipartFile image : images) {
            imageSources.add(
                    convertToImageSource(image));
        }

        return imageSources;
    }

    @Transactional
    public ListingDTO updateListing(UUID listingId, UpdateListingRequest request, List<MultipartFile> newImages, UUID userId) {

        Listing existingListing = listingRepository.findById(listingId)
                .orElseThrow(ListingNotFoundException::new);

        // Security check
        if (!existingListing.getOwner().getId().equals(userId)) {
            throw new UnauthorizedActionException("Listing update unauthorized, action caused by userId: " + userId);
        }

        try {
            handleImageUpdates(listingId, request.getImagePaths(), newImages);

            listingMapper.updateEntityFromRequest(existingListing, request);

            Listing savedListing = listingRepository.save(existingListing);

            eventPublisher.publishEvent(
                    ListingUpdatedEvent.builder().listing(savedListing).build()
            );

            return toDTO(savedListing);

        } catch (Exception e) {
            System.out.println("Error updating listing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /// gets old images, check deleted, add new images
    private void handleImageUpdates(UUID listingId, List<String> keptImageUrls, List<MultipartFile> newImages) {

        try {

            List<String> currentImageUrls = fileStorageService.getListingImagePaths(listingId);

            List<String> imagesToDelete = currentImageUrls.stream()
                    .filter(url -> keptImageUrls == null || !keptImageUrls.contains(url))
                    .toList();

            for (String urlToDelete : imagesToDelete) {
                String filename = urlToDelete.substring(urlToDelete.lastIndexOf("/") + 1);
                try {
                    fileStorageService.deleteImage(listingId, filename);
                } catch (Exception e) {
                    System.out.println("Failed to delete image: " + filename);
                }
            }

            if (newImages != null && !newImages.isEmpty()) {
                List<ImageSource> imageSources = getImageSourceList(newImages);
                fileStorageService.saveImages(imageSources, listingId);
            }
        } catch (Exception e) {
            System.out.println("Error handling image updates: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // clear old, put new
    private void updateTradePreferences(Listing existingListing, List<TradePreferenceDTO> tradePreferenceDTOs) {
        existingListing.getTradePreferences().clear();

        if (tradePreferenceDTOs != null && !tradePreferenceDTOs.isEmpty()) {
            tradePreferenceDTOs.forEach(prefDTO -> {
                TradePreference pref = new TradePreference();
                pref.setDesiredItem(prefDTO.getDesiredItem());
                pref.setExtraAmount(prefDTO.getExtraAmount());
                pref.setPaymentDirection(prefDTO.getPaymentDirection());
                pref.setListing(existingListing);
                existingListing.getTradePreferences().add(pref);
            });
        }
    }

    public List<ListingDTO> getUserListingsWithStatus(UUID ownerId, ListingStatus status) {

        List<Listing> userListings = listingRepository.findAllByOwner_IdAndStatus(ownerId, status);

        if (userListings == null || userListings.isEmpty())
            return List.of();

        return userListings.stream()
                .map(this::toDTO)
                .toList();
    }

    public ListingDTO getListingDTOById(UUID listingId) {

        Listing listing = listingRepository
                .findById(listingId)
                .orElseThrow(ListingNotFoundException::new);

        return toDTO(listing);
    }


    // TODO: remove, admin panel
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
        // listing.setPromotedBy(currentUser.getUsername());
        // listing.setPromotedById(currentUser.getId());
        // listing.setPromotedAt(LocalDateTime.now());

        listingRepository.save(listing);
    }

    public void decreaseItemQuantity(UUID listingId, int quantity) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(ListingNotFoundException::new);

        if (!listing.hasEnoughStock(quantity)) {
            throw new InsufficientStockException();
        }

        listing.setStockQuantity(listing.getStockQuantity() - quantity);

        listingRepository.save(listing);

    }

    public boolean isExistByTitle(String title) {
        return listingRepository.existsByTitle(title);
    }

    public boolean isAvailableForTrade(UUID listingId) {
        return listingRepository.isAvailableForTrade(listingId, ListingStatus.AVAILABLE);
    }

    public String getOwnerUsernameByListingId(UUID listingId) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(ListingNotFoundException::new);

        return listing.getOwner().getUsername();
    }

    public List<ListingDTO> getAllListingDTOs() {

        List<Listing> listings = getAllListings();

        return listings.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<String> getImagePaths(UUID listingId){
        return fileStorageService.getListingImagePaths(listingId);
    }


    public void restoreStock(UUID listingId, int quantity) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found for restock"));

        listing.setStockQuantity(listing.getStockQuantity() + quantity);

        listingRepository.save(listing);

    }

    /**
     * Private helper: fetches images and converts to DTO
     * Centralizes the repetitive pattern
     */
    private ListingDTO toDTO(Listing listing) {

        List<String> imagePaths = getImagePaths(listing);

        if(imagePaths.isEmpty()){
            imagePaths= getPlaceholderImagePath(listing.getMbId());
        }
        return listingMapper.toDTO(listing, imagePaths);
    }

    /**
     * Private helper: if user didnt provide images, fetch placeholder image from mbId
     */
    private List<String> getPlaceholderImagePath(UUID mbId){
        return fileStorageService.getPlaceholderImagePaths(mbId);
    }

    /**
     * Private helper: batch conversion
     */
    private List<ListingDTO> toDTOList(List<Listing> listings) {
        return listings.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Private helper: fetch image paths with fallback epmty list
     */
    private List<String> getImagePaths(Listing listing) {
        List<String> imagePaths = fileStorageService.getListingImagePaths(listing.getId());

        if ((imagePaths == null || imagePaths.isEmpty()) && listing.getMbId() != null) {
            imagePaths = fileStorageService.getListingImagePaths(listing.getMbId());
        }

        return imagePaths != null ? imagePaths : List.of();
    }

}
