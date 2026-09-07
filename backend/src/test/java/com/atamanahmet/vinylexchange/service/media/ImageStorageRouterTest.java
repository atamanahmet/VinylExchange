package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.entity.OrphanedCloudAsset;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import com.atamanahmet.vinylexchange.infrastructure.ImageUploadResult;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import com.atamanahmet.vinylexchange.repository.listing.OrphanedCloudAssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageStorageRouterTest {

    @Mock
    private CloudinaryImageService cloudinaryService;

    @Mock
    private LocalImageService localService;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private OrphanedCloudAssetRepository orphanedCloudAssetRepository;

    @InjectMocks
    private ImageStorageRouter router;

    @Test
    void upload_cloudinarySucceeds_returnsCloudinaryResultsWithoutFallback() throws IOException {
        UUID listingId = UUID.randomUUID();
        List<ImageSource> images = sampleImages();
        List<ImageUploadResult> cloudinaryResults = List.of(
                new ImageUploadResult("cloud-id", "https://cdn.example/a.jpg", 0, StorageProvider.CLOUDINARY));

        when(cloudinaryService.uploadImages(images, listingId)).thenReturn(cloudinaryResults);

        List<ImageUploadResult> results = router.upload(images, listingId);

        assertThat(results).isSameAs(cloudinaryResults);
        verifyNoInteractions(localService);
        verifyNoInteractions(listingRepository);
    }

    @Test
    void upload_cloudinaryFailsWithLocalFallbackDisabled_throwsWithoutCallingLocal() throws IOException {
        ReflectionTestUtils.setField(router, "localFallbackEnabled", false);
        UUID listingId = UUID.randomUUID();
        List<ImageSource> images = sampleImages();

        when(cloudinaryService.uploadImages(images, listingId))
                .thenThrow(new RuntimeException("cloudinary down"));

        assertThatThrownBy(() -> router.upload(images, listingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Image upload failed on all providers");

        verifyNoInteractions(localService);
        verifyNoInteractions(listingRepository);
    }

    @Test
    void upload_cloudinaryFailsWithLocalFallbackEnabled_marksListingForMigrationAndReturnsLocalResults()
            throws IOException {
        ReflectionTestUtils.setField(router, "localFallbackEnabled", true);
        UUID listingId = UUID.randomUUID();
        List<ImageSource> images = sampleImages();
        List<ImageUploadResult> localResults = List.of(
                new ImageUploadResult("local/path.jpg", "http://localhost/path.jpg", 0, StorageProvider.LOCAL));
        Listing listing = Listing.builder().id(listingId).needsImageMigration(false).build();

        when(cloudinaryService.uploadImages(images, listingId))
                .thenThrow(new RuntimeException("cloudinary down"));
        when(localService.uploadImages(images, listingId)).thenReturn(localResults);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        List<ImageUploadResult> results = router.upload(images, listingId);

        assertThat(results).isSameAs(localResults);
        verify(listingRepository).findById(listingId);

        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(listingCaptor.capture());
        assertThat(listingCaptor.getValue().isNeedsImageMigration()).isTrue();
    }

    @Test
    void upload_bothProvidersFailWithLocalFallbackEnabled_throwsWithoutSavingListing() throws IOException {
        ReflectionTestUtils.setField(router, "localFallbackEnabled", true);
        UUID listingId = UUID.randomUUID();
        List<ImageSource> images = sampleImages();

        when(cloudinaryService.uploadImages(images, listingId))
                .thenThrow(new RuntimeException("cloudinary down"));
        when(localService.uploadImages(images, listingId))
                .thenThrow(new RuntimeException("local down"));

        assertThatThrownBy(() -> router.upload(images, listingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Image upload failed on all providers");

        verify(listingRepository, never()).save(any());
    }

    @Test
    void delete_externalProvider_skipsAllDeleteAndOrphanTracking() {
        ListingImage image = listingImage(StorageProvider.EXTERNAL, "https://coverartarchive.org/img.jpg");

        router.delete(image);

        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(localService);
        verifyNoInteractions(orphanedCloudAssetRepository);
    }

    @Test
    void delete_cloudinaryProviderSucceeds_doesNotTrackOrphan() throws IOException {
        ListingImage image = listingImage(StorageProvider.CLOUDINARY, "vinyl/listing-1");

        router.delete(image);

        verify(cloudinaryService).deleteImage(image);
        verify(orphanedCloudAssetRepository, never()).save(any());
    }

    @Test
    void delete_cloudinaryProviderFails_tracksOrphanWithoutThrowing() throws IOException {
        ListingImage image = listingImage(StorageProvider.CLOUDINARY, "vinyl/listing-1");
        doThrow(new IOException("cloudinary delete failed")).when(cloudinaryService).deleteImage(image);

        router.delete(image);

        ArgumentCaptor<OrphanedCloudAsset> assetCaptor = ArgumentCaptor.forClass(OrphanedCloudAsset.class);
        verify(orphanedCloudAssetRepository).save(assetCaptor.capture());
        assertThat(assetCaptor.getValue().getPublicId()).isEqualTo("vinyl/listing-1");
    }

    @Test
    void delete_localProviderFails_doesNotTrackOrphan() throws IOException {
        ListingImage image = listingImage(StorageProvider.LOCAL, "listings/abc.jpg");
        doThrow(new IOException("local delete failed")).when(localService).deleteImage(image);

        router.delete(image);

        verify(localService).deleteImage(image);
        verify(orphanedCloudAssetRepository, never()).save(any());
    }

    @Test
    void deleteAll_routesEachImageByItsProvider() throws IOException {
        ListingImage cloudImage = listingImage(StorageProvider.CLOUDINARY, "vinyl/cloud-1");
        ListingImage localImage = listingImage(StorageProvider.LOCAL, "listings/local-1");

        router.deleteAll(List.of(cloudImage, localImage));

        verify(cloudinaryService).deleteImage(cloudImage);
        verify(localService).deleteImage(localImage);
    }

    private static List<ImageSource> sampleImages() {
        return List.of(new ImageSource(
                new byte[] {1},
                "cover.jpg",
                "image/jpeg",
                1));
    }

    private static ListingImage listingImage(StorageProvider provider, String publicId) {
        return ListingImage.builder()
                .id(UUID.randomUUID())
                .provider(provider)
                .publicId(publicId)
                .secureUrl(publicId)
                .position(0)
                .build();
    }
}
