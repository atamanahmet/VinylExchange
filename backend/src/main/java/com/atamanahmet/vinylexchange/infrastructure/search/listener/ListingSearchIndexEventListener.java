package com.atamanahmet.vinylexchange.infrastructure.search.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.atamanahmet.vinylexchange.infrastructure.search.service.OpenSearchIndexService;
import com.atamanahmet.vinylexchange.event.ListingCreatedEvent;
import com.atamanahmet.vinylexchange.event.ListingUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ListingSearchIndexEventListener {

    private final OpenSearchIndexService openSearchIndexService;

    @Async
    @TransactionalEventListener(classes = ListingCreatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void onListingCreated(ListingCreatedEvent creationEvent) {
        openSearchIndexService.indexListing(creationEvent.getListing());
    }

    @Async
    @TransactionalEventListener(classes = ListingUpdatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void onListingUpdated(ListingUpdatedEvent updateEvent) {
        openSearchIndexService.indexListing(updateEvent.getListing());
    }

}
