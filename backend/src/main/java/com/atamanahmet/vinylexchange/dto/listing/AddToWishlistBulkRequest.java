package com.atamanahmet.vinylexchange.dto.listing;

import java.util.List;

public record AddToWishlistBulkRequest(
        List<AddToWishlistRequest> bulkRequest
) {}
