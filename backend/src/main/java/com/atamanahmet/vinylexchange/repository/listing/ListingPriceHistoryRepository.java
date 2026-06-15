package com.atamanahmet.vinylexchange.repository.listing;

import com.atamanahmet.vinylexchange.domain.entity.ListingPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingPriceHistoryRepository extends JpaRepository<ListingPriceHistory, UUID> {

    /**
     * Returns full price history for a listing, newest first
     * Used for buyer-visible price change log and seller audit
     */
    List<ListingPriceHistory> findAllByListingIdOrderByOccurredAtDesc(UUID listingId);
}