package com.atamanahmet.vinylexchange.service.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.atamanahmet.vinylexchange.infrastructure.CompressedImage;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload-listing-dir}")
    private String UPLOAD_LISTING_DIR;

    @Value("${file.upload-placeholder-dir}")
    private String UPLOAD_PLACEHOLDER_DIR;

    @Value("${app.base-url}")
    private String BASE_URL;

    private final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final ImageCompressionService imageCompressionService;
    private final WebClient webClient;

    // *
    // For users that didnt upload any image,
    // coverart-archive placeholder downloader
    // */
    public List<String> saveImageFromUrl(String imageUrl, UUID listingId) throws IOException {

        if (imageUrl == null) {
            return null;
        }

        ImageSource imageSource = downloadExternalImage(imageUrl);

        List<CompressedImage> compressedImages = imageCompressionService.compressImages(List.of(imageSource));

//        if (imageSource == null) {
//            return List.of();
//        }

        return saveCompressedImages(compressedImages, listingId);

    }

    public List<String> saveImages(List<ImageSource> images, UUID listingId) throws IOException {

        if (images == null) {
            return null;
        }

        List<CompressedImage> compressedImages = imageCompressionService.compressImages(images);

        return saveCompressedImages(compressedImages, listingId);

    }

    public List<String> saveCompressedImages(
            List<CompressedImage> compressedImages,
            UUID listingId) throws IOException {

        Path listingFolder = Paths.get(UPLOAD_LISTING_DIR).resolve(listingId.toString()).toAbsolutePath();

        List<String> savedPaths = new ArrayList<>();

        for (CompressedImage image : compressedImages) {

            Path path = listingFolder.resolve(image.getFileName());

            Files.createDirectories(listingFolder);

            try {

                Files.write(path, image.getImage());

                String fullPath = BASE_URL + "/uploads/listings/"
                        + listingId + "/" + image.getFileName();

                savedPaths.add(fullPath);

            } catch (IOException e) {
                System.out.println("Directory creating or file write exception: " + e.getMessage());
                throw new RuntimeException("COmpressed images save failed: " + e.getMessage());
            }
        }
        return savedPaths;
    }

    public List<String> getListingImagePaths(UUID listingId) {

        Path listingFolder = Paths.get(UPLOAD_LISTING_DIR).resolve(listingId.toString()).toAbsolutePath();

        if (!Files.exists(listingFolder)) {
            logger.debug("No image folder found for listing: {}", listingId);
            return Collections.emptyList();
        }

        // mapping full path urls for frontend
        try (Stream<Path> paths = Files.list(listingFolder)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> isImageFile(path.getFileName().toString()))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted()
                    .map(filename -> BASE_URL + "/uploads/listings/" + listingId + "/" + filename)

                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to read images for listing {}: {}", listingId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void deleteImage(UUID listingId, String filename) throws IOException {
        Path imagePath = Paths.get(UPLOAD_LISTING_DIR)
                .resolve(listingId.toString())
                .resolve(filename)
                .toAbsolutePath();

        if (!imagePath.startsWith(Paths.get(UPLOAD_LISTING_DIR).toAbsolutePath())) {
            throw new SecurityException("Invalid file path");
        }

        Files.deleteIfExists(imagePath);
        logger.info("Deleted image {} for listing {}", filename, listingId);
    }

    public List<CompressedImage> compressImages(List<ImageSource> images) {
        try {
            return imageCompressionService.compressImages(images);

        } catch (IOException e) {
            logger.error("Error while compressing images: " + e.getMessage());
            throw new RuntimeException("Image compression failed;");
        }
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") ||
                lower.endsWith(".png") ||
                lower.endsWith(".webp") ||
                lower.endsWith(".gif");
    }

    public void deleteListingImages(UUID listingId) {

        Path listingFolder = Paths.get(UPLOAD_LISTING_DIR).resolve(listingId.toString()).toAbsolutePath();

        if (!Files.exists(listingFolder)) {
            logger.debug("No images to delete for listing: {}", listingId);
            return;
        }

        try (Stream<Path> paths = Files.walk(listingFolder)) {
            paths.sorted(Comparator.reverseOrder()) // deepest first
                    .forEach(path -> {
                        try {
                            FileUtils.deleteDirectory(listingFolder.toFile());
                            logger.info("Force deleted all images for listing: {}", listingId);
                        } catch (IOException e) {
                            logger.warn("Failed to force delete images for listing {}: {}", listingId, e.getMessage());
                        }
                    });
            logger.info("Deleted all images for listing: {}", listingId);
        } catch (IOException e) {
            logger.error("Failed to walk directory for listing {}: {}", listingId, e.getMessage());
        }
    }

    public String getMainImagePath(UUID listingId) {

        Path listingFolder = Paths.get(UPLOAD_LISTING_DIR).resolve(listingId.toString()).toAbsolutePath();

        if (!Files.exists(listingFolder)) {
            logger.debug("No image folder found for listing: {}", listingId);
            return null;
        }

        try (Stream<Path> paths = Files.list(listingFolder)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> isImageFile(path.getFileName().toString()))
                    .sorted()
                    .findFirst() // first image only as a mainImg
                    .map(path -> BASE_URL + "/uploads/listings/" + listingId + "/" + path.getFileName().toString())
                    .orElse(null); // null if no images found
        } catch (IOException e) {
            logger.error("read eror for main image {}: {}", listingId, e.getMessage());
            return null;
        }
    }

    public String readTextContentFile(String filePath) {
        try {
            String relativePath = filePath.substring(filePath.indexOf("/uploads/") + 1);

            Path path = Paths.get(relativePath);

            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("COntent file read error");
        }
    }

    public ImageSource downloadExternalImage(String imageUrl) {

        try {
            byte[] bytes = webClient.get()
                    .uri(imageUrl)
                    .accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(10))
                    .retry(3)
                    .block();

            if (bytes == null || bytes.length == 0)
                return null;

            return new ImageSource(
                    new ByteArrayInputStream(bytes),
                    "cover.jpg",
                    "image/jpeg",
                    bytes.length);

        } catch (Exception e) {
            logger.warn("Image download failed: {}", imageUrl);
            return null;
        }
    }

    public void savePlaceholderImage(ImageSource imageSource, UUID mbId) throws IOException {

        if (imageSource == null) {
            return;
        }

        Path placeholderFolder = Paths.get(UPLOAD_PLACEHOLDER_DIR)
                .resolve(mbId.toString())
                .toAbsolutePath();

        if (Files.exists(placeholderFolder)) {
            return;
        }

        Files.createDirectories(placeholderFolder);

        String filename = imageSource.getOriginalFilename();

        Path filePath = placeholderFolder.resolve(filename);

        Files.copy(imageSource.getInputStream(), filePath);

        String fullPath = BASE_URL + "/uploads/placeholders/"
                + mbId + "/" + filename;

        logger.info("Placeholder coverArt downloaded for mb id: {}", mbId);

    }

    public List<String> getPlaceholderImagePaths(UUID mbId) {

        if(mbId==null){
            return Collections.emptyList();
        }

        Path folder = Paths.get(UPLOAD_PLACEHOLDER_DIR)
                .resolve(mbId.toString())
                .toAbsolutePath();

        if (!Files.exists(folder)) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(folder)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> isImageFile(path.getFileName().toString()))
                    .sorted()
                    .map(path -> BASE_URL + "/uploads/placeholders/"
                            + mbId + "/"
                            + path.getFileName().toString())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Failed to read placeholder images for mbId {}: {}", mbId, e.getMessage());
            return Collections.emptyList();
        }
    }




}
