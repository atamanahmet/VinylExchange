package com.atamanahmet.vinylexchange.dto.order;

import java.util.List;

/**
 * Returned after checkout, one order per seller in the cart
 */
public record CheckoutResponseDTO(
        List<OrderDTO> orders,
        int orderCount
) {}