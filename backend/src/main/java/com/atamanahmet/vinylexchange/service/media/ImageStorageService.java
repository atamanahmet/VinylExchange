package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImageStorageService {

    /**
     * Upload images and return unified results
     */
    List<ImageUploadResult> uploadImages(List<ImageSource> images, UUID listingId) throws IOException;

    /**
     * Delete single image by its stored publicId
     */
    void deleteImage(ListingImage image) throws IOException;

    /**
     * Delete all images belonging to a listing
     */
    void deleteAllImages(List<ListingImage> images);
}