package com.atamanahmet.vinylexchange.dto.order;

import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.OrderViewerRole;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderDTO(
        UUID orderId,
        Long orderNumber,
        OrderStatus status,
        SaleType saleType,
        String buyerUsername,
        String sellerUsername,
        OrderViewerRole viewerRole,
        Long totalPriceKurus,
        List<OrderItemDTO> items,
        LocalDateTime shippingDeadline,
        LocalDateTime expectedDeliveryDate,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        String shippingAddressSummary,
        String shipmentHandlerCode,
        String shipmentBarcode,
        String shipmentLabelUrl,
        LocalDateTime shipmentLabelGeneratedAt
) {}