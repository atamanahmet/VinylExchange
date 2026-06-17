package com.atamanahmet.vinylexchange.dto.listing;

import jakarta.validation.constraints.NotBlank;

public record AddToWishlistRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Artist is required") String artist,
        Integer year,
        String country,
        String label,
        String barcode,
        String externalCoverUrl,
        String format
) {}
