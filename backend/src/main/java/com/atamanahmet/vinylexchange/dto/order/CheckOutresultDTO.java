package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

public record CheckOutresultDTO(
        boolean success,
        String message,
        UUID orderId
) {}
