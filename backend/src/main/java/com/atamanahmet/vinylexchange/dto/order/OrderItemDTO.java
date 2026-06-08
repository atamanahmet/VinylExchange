package com.atamanahmet.vinylexchange.dto.order;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderItemDTO(
        UUID listingId,
        String listingTitle,
        String listingMainImageUrl,
        Long unitPriceKurus,
        int quantity,
        Long subTotalKurus
) {}