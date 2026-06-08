package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageRouter {

    private final CloudinaryImageService cloudinaryService;
    private final LocalImageService localService;

    /**
     * Try cloudinary first, fall back to local on failure
     */
    public List<ImageUploadResult> upload(List<ImageSource> images, UUID listingId) {
        try {
            return cloudinaryService.uploadImages(images, listingId);
        } catch (Exception e) {
            log.warn("Cloudinary upload failed, falling back to local: {}", e.getMessage());
            try {
                return localService.uploadImages(images, listingId);
            } catch (Exception ex) {
                log.error("Local upload also failed for listing {}: {}", listingId, ex.getMessage());
                throw new RuntimeException("Image upload failed on all providers", ex);
            }
        }
    }

    /**
     * Routes delete to correct provider based on stored provider field
     */
    public void delete(ListingImage image) {
        try {
            if (image.getProvider() == StorageProvider.CLOUDINARY) {
                cloudinaryService.deleteImage(image);
            } else {
                localService.deleteImage(image);
            }
        } catch (Exception e) {
            log.warn("Failed to delete image {} from {}: {}",
                    image.getPublicId(), image.getProvider(), e.getMessage());
        }
    }

    /**
     * Delete all images, routes each by its own provider
     */
    public void deleteAll(List<ListingImage> images) {
        images.forEach(this::delete);
    }
}