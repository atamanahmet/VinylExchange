package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.CompressedImage;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalImageService implements ImageStorageService {

    private final FileStorageService fileStorageService;
    private final ImageCompressionService imageCompressionService;

    @Override
    public List<ImageUploadResult> uploadImages(List<ImageSource> images, UUID listingId) throws IOException {
        List<ImageUploadResult> results = new ArrayList<>();

        List<CompressedImage> compressed = imageCompressionService.compressImages(images);
        List<String> savedPaths = fileStorageService.saveCompressedImages(compressed, listingId);

        for (int i = 0; i < savedPaths.size(); i++) {
            String path = savedPaths.get(i);
            /** publicId and secureUrl are same for local, path is the identifier */
            results.add(new ImageUploadResult(path, path, i, StorageProvider.LOCAL));
        }

        return results;
    }

    @Override
    public void deleteImage(ListingImage image) throws IOException {
        String filename = image.getPublicId()
                .substring(image.getPublicId().lastIndexOf("/") + 1);
        /** extract listingId from path: /uploads/listings/{listingId}/filename */
        String[] parts = image.getPublicId().split("/");
        UUID listingId = UUID.fromString(parts[parts.length - 2]);
        fileStorageService.deleteImage(listingId, filename);
        log.info("Deleted local image: {}", image.getPublicId());
    }

    @Override
    public void deleteAllImages(List<ListingImage> images) {
        images.forEach(image -> {
            try {
                deleteImage(image);
            } catch (IOException e) {
                log.warn("Failed to delete local image: {}", image.getPublicId());
            }
        });
    }
}