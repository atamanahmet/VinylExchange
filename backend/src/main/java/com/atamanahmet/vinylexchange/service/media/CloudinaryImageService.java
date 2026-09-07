package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryImageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    @Override
    public List<ImageUploadResult> uploadImages(List<ImageSource> images, UUID listingId) throws IOException {
        List<ImageUploadResult> results = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            ImageSource image = images.get(i);

            Transformation transformation = new Transformation().width(1200).crop("limit");

            Map params = ObjectUtils.asMap(
                    "quality", "auto",
                    "fetch_format", "auto",
                    "flags", "progressive",
                    "transformation", transformation
            );

            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(image.getData(), params);

            String publicId = uploadResult.get("public_id").toString();
            String secureUrl = uploadResult.get("secure_url").toString();

            results.add(new ImageUploadResult(publicId, secureUrl, i, StorageProvider.CLOUDINARY));
        }

        return results;
    }

    public void deleteByPublicId(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("Deleted cloudinary image: {}", publicId);
    }

    @Override
    public void deleteImage(ListingImage image) throws IOException {
        deleteByPublicId(image.getPublicId());
    }

    @Override
    public void deleteAllImages(List<ListingImage> images) {
        images.forEach(image -> {
            try {
                deleteImage(image);
            } catch (IOException e) {
                log.warn("Failed to delete cloudinary image: {}", image.getPublicId());
            }
        });
    }
}