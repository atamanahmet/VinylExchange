package com.atamanahmet.vinylexchange.repository.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {

    List<ListingImage> findByListing_IdAndProvider(UUID listingId, StorageProvider provider);
}
