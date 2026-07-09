package com.atamanahmet.vinylexchange.mapper;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderItem;
import com.atamanahmet.vinylexchange.domain.enums.OrderViewerRole;
import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.atamanahmet.vinylexchange.dto.order.CheckoutResponseDTO;
import com.atamanahmet.vinylexchange.dto.order.OrderDTO;
import com.atamanahmet.vinylexchange.dto.order.OrderItemDTO;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderDTO toDTO(Order order, UUID authenticatedUserId) {
        return toDTO(order, Map.of(), Map.of(), authenticatedUserId);
    }

    public static OrderDTO toDTO(
            Order order,
            Map<UUID, String> listingPublicIds,
            UUID authenticatedUserId) {
        return toDTO(order, listingPublicIds, Map.of(), authenticatedUserId);
    }

    public static OrderDTO toDTO(
            Order order,
            Map<UUID, String> listingPublicIds,
            Map<UUID, String> usernames,
            UUID authenticatedUserId) {
        return OrderDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .saleType(order.getSaleType())
                .buyerUsername(usernames.get(order.getBuyerId()))
                .sellerUsername(usernames.get(order.getSellerId()))
                .viewerRole(resolveViewerRole(order, authenticatedUserId))
                .totalPriceKurus(order.getTotalPrice())
                .items(order.getOrderItems().stream()
                        .map(item -> toItemDTO(item, listingPublicIds.get(item.getListingId())))
                        .toList())
                .shippingDeadline(order.getShippingDeadline())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .shippingAddressSummary(AddressSnapshotFormatter.toSummary(order.getShippingAddressSnapshot()))
                .shipmentHandlerCode(order.getShipmentHandlerCode())
                .shipmentBarcode(order.getShipmentBarcode())
                .shipmentLabelUrl(order.getShipmentLabelUrl())
                .shipmentLabelGeneratedAt(order.getShipmentLabelGeneratedAt())
                .build();
    }

    public static CheckoutResponseDTO toCheckoutResponse(
            List<Order> orders,
            Map<UUID, String> listingPublicIds,
            Map<UUID, String> usernames,
            UUID authenticatedUserId,
            AddressSnapshot shippingAddressSnapshot) {
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> toDTO(order, listingPublicIds, usernames, authenticatedUserId))
                .toList();
        return new CheckoutResponseDTO(
                orderDTOs,
                orderDTOs.size(),
                AddressSnapshotFormatter.toCheckoutSummary(shippingAddressSnapshot));
    }

    static OrderViewerRole resolveViewerRole(Order order, UUID authenticatedUserId) {
        if (order.getBuyerId().equals(authenticatedUserId)) {
            return OrderViewerRole.BUYER;
        }
        if (order.getSellerId().equals(authenticatedUserId)) {
            return OrderViewerRole.SELLER;
        }
        throw new UnauthorizedActionException("User is not a participant in this order");
    }

    private static OrderItemDTO toItemDTO(OrderItem item, String publicId) {
        return OrderItemDTO.builder()
                .publicId(publicId)
                .listingTitle(item.getListingTitle())
                .listingMainImageUrl(item.getListingMainImageUrl())
                .unitPriceKurus(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subTotalKurus(item.getSubTotal())
                .build();
    }
}
