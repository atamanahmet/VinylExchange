package com.atamanahmet.vinylexchange.service.listing;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingPriceHistory;
import com.atamanahmet.vinylexchange.repository.listing.ListingPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Owns all reads and writes to listing_price_history table
 */
@Service
@RequiredArgsConstructor
public class ListingPriceHistoryService {

    private final ListingPriceHistoryRepository listingPriceHistoryRepository;

    /**
     * Records the initial price entry when a listing is first created
     * Old values are null because no previous price exists
     */
    public void recordCreation(Listing listing, String ownerIdString) {
        listingPriceHistoryRepository.save(ListingPriceHistory.builder()
                .listingId(listing.getId())
                .changedBy(ownerIdString)
                .oldPriceKurus(null)
                .newPriceKurus(listing.getPriceKurus())
                .oldSellerEarningsKurus(null)
                .newSellerEarningsKurus(listing.getSellerEarningsKurus())
                .oldPlatformCutKurus(null)
                .newPlatformCutKurus(listing.getPlatformCutKurus())
                .feeBpAtChange(listing.getPlatformFeeBP())
                .occurredAt(LocalDateTime.now())
                .note("Initial listing creation")
                .build());
    }

    /**
     * Records a price change on an existing listing
     * Old values captured before the update is applied
     */
    public void recordUpdate(Listing savedListing, long oldPrice, long oldSellerEarnings,
                             long oldPlatformCut, String changedBy) {
        listingPriceHistoryRepository.save(ListingPriceHistory.builder()
                .listingId(savedListing.getId())
                .changedBy(changedBy)
                .oldPriceKurus(oldPrice)
                .newPriceKurus(savedListing.getPriceKurus())
                .oldSellerEarningsKurus(oldSellerEarnings)
                .newSellerEarningsKurus(savedListing.getSellerEarningsKurus())
                .oldPlatformCutKurus(oldPlatformCut)
                .newPlatformCutKurus(savedListing.getPlatformCutKurus())
                .feeBpAtChange(savedListing.getPlatformFeeBP())
                .occurredAt(LocalDateTime.now())
                .note("Seller updated price")
                .build());
    }

    /**
     * Returns full price history for a listing, newest first
     * Used for buyer-visible price log and admin audit panel
     */
    public List<ListingPriceHistory> getHistoryForListing(UUID listingId) {
        return listingPriceHistoryRepository.findAllByListingIdOrderByOccurredAtDesc(listingId);
    }
}