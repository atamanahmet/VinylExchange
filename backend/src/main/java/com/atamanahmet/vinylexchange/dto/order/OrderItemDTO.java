package com.atamanahmet.vinylexchange.dto.order;

import lombok.Builder;

@Builder
public record OrderItemDTO(
        String publicId,
        String listingTitle,
        String listingMainImageUrl,
        Long unitPriceKurus,
        int quantity,
        Long subTotalKurus
) {}