package com.atamanahmet.vinylexchange.mapper;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderItem;
import com.atamanahmet.vinylexchange.dto.order.CheckoutResponseDTO;
import com.atamanahmet.vinylexchange.dto.order.OrderDTO;
import com.atamanahmet.vinylexchange.dto.order.OrderItemDTO;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .saleType(order.getSaleType())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .totalPriceKurus(order.getTotalPrice())
                .items(order.getOrderItems().stream()
                        .map(OrderMapper::toItemDTO)
                        .toList())
                .shippingDeadline(order.getShippingDeadline())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .build();
    }

    public static CheckoutResponseDTO toCheckoutResponse(List<Order> orders) {
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderMapper::toDTO)
                .toList();
        return new CheckoutResponseDTO(orderDTOs, orderDTOs.size());
    }

    private static OrderItemDTO toItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .listingId(item.getListingId())
                .listingTitle(item.getListingTitle())
                .listingMainImageUrl(item.getListingMainImageUrl())
                .unitPriceKurus(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subTotalKurus(item.getSubTotal())
                .build();
    }
}