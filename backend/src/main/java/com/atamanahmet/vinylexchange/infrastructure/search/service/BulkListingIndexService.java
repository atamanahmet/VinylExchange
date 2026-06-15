package com.atamanahmet.vinylexchange.infrastructure.search.service;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.service.listing.ListingService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * One-time backfill tool. Re-indexes all listings from Postgres into OpenSearch.
 * Used on fresh deploy or after OpenSearch downtime to restore the index.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkListingIndexService {

    private final ListingService listingService;
    private final OpenSearchIndexService openSearchIndexService;
    private final SearchHealthIndicator searchHealthIndicator;

    private static final int BATCH_SIZE = 100;

    public void indexAllListings() {

        if (!searchHealthIndicator.isOpenSearchAvailable()) {
            log.info("bulk_index_skipped reason=opensearch_unavailable");
            return;
        }

        log.info("Bulk indexing started");

        long totalCount = listingService.totalCount();

        log.info("Total listings count: {}", totalCount);

        int pageNumber = 0;

        Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);

        Page<Listing> page;

        do {
            page = listingService.getAllListingsPageable(pageable);

            log.info("Processing page {}/{}", pageNumber + 1, page.getTotalPages());

            page.getContent().forEach(openSearchIndexService::indexListing);

            pageable = pageable.next();
            pageNumber++;

        } while (page.hasNext());

        log.info("Bulk indexing complete for count: {} listings", totalCount);
    }
}