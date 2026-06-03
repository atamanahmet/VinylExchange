package com.atamanahmet.vinylexchange.dto.listing;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToWishlistRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Artist is required")
    private String artist;

    private Integer year;

    private String country;

    private String label;

    private String barcode;

    private String externalCoverUrl;

    private String format;
}
