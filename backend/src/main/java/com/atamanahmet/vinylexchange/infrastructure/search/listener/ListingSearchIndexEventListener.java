package com.atamanahmet.vinylexchange.infrastructure.search.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.atamanahmet.vinylexchange.service.listing.ListingIndexService;
import com.atamanahmet.vinylexchange.event.ListingCreatedEvent;
import com.atamanahmet.vinylexchange.event.ListingUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ListingSearchIndexEventListener {

    private final ListingIndexService listingIndexService;

    @Async
    @TransactionalEventListener(classes = ListingCreatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void onListingCreated(ListingCreatedEvent creationEvent) {
        listingIndexService.indexListing(creationEvent.getListing());
    }

    @Async
    @TransactionalEventListener(classes = ListingUpdatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void onListingUpdated(ListingUpdatedEvent updateEvent) {
        listingIndexService.indexListing(updateEvent.getListing());
    }

}
