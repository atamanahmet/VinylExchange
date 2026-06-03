package com.atamanahmet.vinylexchange.event;

import com.atamanahmet.vinylexchange.domain.entity.Listing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListingUpdatedEvent {
    private final Listing listing;
}
