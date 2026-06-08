package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class CloudinaryImageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryImageService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    @Override
    public List<ImageUploadResult> uploadImages(List<ImageSource> images, UUID listingId) throws IOException {
        List<ImageUploadResult> results = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            ImageSource image = images.get(i);

            Map params = ObjectUtils.asMap(
                    "quality", "auto",
                    "fetch_format", "auto",
                    "flags", "progressive",
                    "transformation", ObjectUtils.asMap(
                            "width", 1200,
                            "crop", "limit"
                    )
            );

            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(image.getInputStream(), params);

            String publicId = uploadResult.get("public_id").toString();
            String secureUrl = uploadResult.get("secure_url").toString();

            results.add(new ImageUploadResult(publicId, secureUrl, i, StorageProvider.CLOUDINARY));
        }

        return results;
    }

    @Override
    public void deleteImage(ListingImage image) throws IOException {
        cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
        log.info("Deleted cloudinary image: {}", image.getPublicId());
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