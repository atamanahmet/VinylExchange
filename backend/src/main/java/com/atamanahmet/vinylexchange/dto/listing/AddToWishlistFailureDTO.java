package com.atamanahmet.vinylexchange.dto.listing;

public record AddToWishlistFailureDTO(
        AddToWishlistRequest request,
        String reason
) {}
