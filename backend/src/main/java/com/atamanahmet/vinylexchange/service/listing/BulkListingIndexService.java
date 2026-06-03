package com.atamanahmet.vinylexchange.service.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.entity.Listing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO: devonly
public class BulkListingIndexService {

    private final ListingService listingService;
    private final ListingIndexService listingIndexService;

    private static final int BATCH_SIZE = 100;

    public void indexAllListings() {

        log.info("Bulk indexing started");

        long totalCount = listingService.totalCount();

        log.info("Total listings count: {}", totalCount);

        int pageNumber = 0;

        Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);

        Page<Listing> page;

        do {
            page = listingService.getAllListingsPageable(pageable);

            log.info("Processing page {}/{}", pageNumber + 1, page.getTotalPages());

            page.getContent().forEach(listing -> {
                listingIndexService.indexListing(listing);
            });

            pageable = pageable.next();
            pageNumber++;

        } while (page.hasNext());

        log.info("Bulk indexing complete for count: {} listings", totalCount);
    }
}