package com.atamanahmet.vinylexchange.dto.listing;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistItemDTO {

    private UUID id;

    private String title;

    private String artist;

    private Integer year;

    private String format;

    private String country;

    private String externalCoverUrl;

    private String barcode;

    private String label;

    private LocalDateTime addedAt;

}
