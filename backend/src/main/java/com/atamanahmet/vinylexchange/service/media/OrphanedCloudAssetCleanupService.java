package com.atamanahmet.vinylexchange.service.media;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.entity.OrphanedCloudAsset;
import com.atamanahmet.vinylexchange.repository.listing.OrphanedCloudAssetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrphanedCloudAssetCleanupService {

    private final OrphanedCloudAssetRepository orphanedCloudAssetRepository;
    private final CloudinaryImageService cloudinaryService;

    @Scheduled(fixedDelay = 300000)
    public void cleanupOrphanedCloudAssets() {
        List<OrphanedCloudAsset> assets = orphanedCloudAssetRepository.findAll();

        for (OrphanedCloudAsset asset : assets) {
            try {
                cloudinaryService.deleteByPublicId(asset.getPublicId());
                orphanedCloudAssetRepository.delete(asset);
            } catch (Exception e) {
                log.warn("Failed to delete orphaned cloud asset {}: {}",
                        asset.getPublicId(), e.getMessage());
            }
        }
    }
}
