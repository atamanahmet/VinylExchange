package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.entity.OrphanedCloudAsset;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import com.atamanahmet.vinylexchange.repository.listing.OrphanedCloudAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageRouter {

    private final CloudinaryImageService cloudinaryService;
    private final LocalImageService localService;
    private final ListingRepository listingRepository;
    private final OrphanedCloudAssetRepository orphanedCloudAssetRepository;

    @Value("${app.storage.local-fallback-enabled:false}")
    private boolean localFallbackEnabled;

    public List<ImageUploadResult> upload(List<ImageSource> images, UUID listingId) {
        try {
            return cloudinaryService.uploadImages(images, listingId);
        } catch (Exception e) {
            if (!localFallbackEnabled) {
                throw new RuntimeException("Image upload failed on all providers", e);
            }
            log.warn("Cloudinary upload failed, falling back to local: {}", e.getMessage());
            List<ImageUploadResult> results;
            try {
                results = localService.uploadImages(images, listingId);
            } catch (Exception ex) {
                log.error("Local upload also failed for listing {}: {}", listingId, ex.getMessage());
                throw new RuntimeException("Image upload failed on all providers", ex);
            }
            Listing listing = listingRepository.findById(listingId)
                    .orElseThrow(() -> new RuntimeException("Listing not found: " + listingId));
            listing.setNeedsImageMigration(true);
            listingRepository.save(listing);
            return results;
        }
    }

    /**
     * Routes delete to correct provider based on stored provider field
     */
    public void delete(ListingImage image) {
        if (image.getProvider() == StorageProvider.EXTERNAL) {
            return;
        }
        try {
            if (image.getProvider() == StorageProvider.CLOUDINARY) {
                cloudinaryService.deleteImage(image);
            } else {
                localService.deleteImage(image);
            }
        } catch (Exception e) {
            log.warn("Failed to delete image {} from {}: {}",
                    image.getPublicId(), image.getProvider(), e.getMessage());
            if (image.getProvider() == StorageProvider.CLOUDINARY) {
                orphanedCloudAssetRepository.save(OrphanedCloudAsset.builder()
                        .publicId(image.getPublicId())
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    /**
     * Delete all images, routes each by its own provider
     */
    public void deleteAll(List<ListingImage> images) {
        images.forEach(this::delete);
    }
}
