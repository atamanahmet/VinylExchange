package com.atamanahmet.vinylexchange.infrastructure.search.document;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListingDocument {

    private UUID id;

    private String title;

    private String artistName;

    private Integer year;

    private String status;

    private String country;

    private Long price;

    private String format;

    private String labelName;

    private String barcode;

    private String ownerUsername;

    private LocalDateTime createdAt;

    private Boolean isPromoted;

}
