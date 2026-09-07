package com.atamanahmet.vinylexchange.repository.listing;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.entity.OrphanedCloudAsset;

@Repository
public interface OrphanedCloudAssetRepository extends JpaRepository<OrphanedCloudAsset, UUID> {
}
