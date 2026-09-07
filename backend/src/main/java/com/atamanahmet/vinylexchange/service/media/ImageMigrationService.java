package com.atamanahmet.vinylexchange.service.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.atamanahmet.vinylexchange.repository.listing.ListingImageRepository;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageMigrationService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final CloudinaryImageService cloudinaryService;
    private final LocalImageService localService;

    @Value("${file.upload-listing-dir}")
    private String uploadListingDir;

    @Scheduled(fixedDelay = 300000)
    public void migrateLocalImages() {
        List<Listing> listings = listingRepository.findByNeedsImageMigrationTrue();

        for (Listing listing : listings) {
            List<ListingImage> localImages = listingImageRepository.findByListing_IdAndProvider(
                    listing.getId(), StorageProvider.LOCAL);
            boolean allMigrated = true;

            for (ListingImage image : localImages) {
                try {
                    migrateImage(listing, image);
                } catch (Exception e) {
                    log.error("Failed to migrate image {} for listing {}: {}",
                            image.getId(), listing.getId(), e.getMessage());
                    allMigrated = false;
                }
            }

            if (allMigrated) {
                listing.setNeedsImageMigration(false);
                listingRepository.save(listing);
            }
        }
    }

    private void migrateImage(Listing listing, ListingImage image) throws IOException {
        Path filePath = resolveLocalImagePath(image.getPublicId());

        String filename = filePath.getFileName().toString();
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        long size = Files.size(filePath);

        ImageUploadResult result;
        byte[] data = Files.readAllBytes(filePath);
        ImageSource source = new ImageSource(data, filename, contentType, size);
        result = cloudinaryService.uploadImages(List.of(source), listing.getId()).get(0);

        localService.deleteImage(image);

        image.setPublicId(result.getPublicId());
        image.setSecureUrl(result.getSecureUrl());
        image.setProvider(StorageProvider.CLOUDINARY);
        image.setUploadedAt(LocalDateTime.now());
        listingImageRepository.save(image);
    }

    private Path resolveLocalImagePath(String publicId) {
        Path baseDir = Paths.get(uploadListingDir).toAbsolutePath().normalize();

        Path resolved;
        if (publicId.contains("://")) {
            String filename = publicId.substring(publicId.lastIndexOf('/') + 1);
            String[] parts = publicId.split("/");
            String listingSegment = parts[parts.length - 2];
            resolved = baseDir.resolve(listingSegment).resolve(filename).normalize();
        } else {
            resolved = baseDir.resolve(publicId).normalize();
        }

        if (!resolved.startsWith(baseDir)) {
            throw new SecurityException("Invalid file path");
        }
        return resolved;
    }
}
