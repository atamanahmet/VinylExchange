package com.atamanahmet.vinylexchange.service.shipment;

import java.time.LocalDateTime;

import com.atamanahmet.vinylexchange.infrastructure.shipment.ShipmentProvider;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.atamanahmet.vinylexchange.service.user.UserAddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentProvider shipmentProvider;
    private final UserAddressService userAddressService;

    /**
     * Creates a shipment for a paid order. Deserializes address snapshots, builds
     * request, calls provider, writes shipment fields back to order. Returns updated
     * order.
     */
    public Order createShipmentForOrder(Order order, String handlerCode, UserAddress sellerAddress) {
        AddressSnapshot buyerSnapshot = order.getShippingAddressSnapshot();
        if (buyerSnapshot == null) {
            throw new IllegalStateException("Address snapshot is missing on order");
        }
        AddressSnapshot sellerSnapshot = userAddressService.toSnapshot(sellerAddress);
        if (sellerSnapshot == null) {
            throw new IllegalStateException("Seller address snapshot could not be built");
        }

        order.setSellerAddressSnapshot(sellerSnapshot);

        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .handlerCode(handlerCode)
                .orderReference(order.getOrderNumber().toString())
                .senderName(sellerSnapshot.fullName())
                .senderPhone(sellerSnapshot.phone())
                .senderCity(sellerSnapshot.city())
                .senderDistrict(sellerSnapshot.district())
                .senderAddress(sellerSnapshot.addressLine())
                .recipientName(buyerSnapshot.fullName())
                .recipientPhone(buyerSnapshot.phone())
                .recipientCity(buyerSnapshot.city())
                .recipientDistrict(buyerSnapshot.district())
                .recipientAddress(buyerSnapshot.addressLine())
                .packageHeight(10)
                .packageWidth(15)
                .packageDepth(5)
                .packageWeight(1)
                .build();

        log.info("Creating shipment shipment for order {} handler {}", order.getId(), handlerCode);

        CreateShipmentResponse response = shipmentProvider.createShipment(request);

        order.setShipmentOrderId(response.getShipmentOrderId());
        order.setShipmentBarcode(response.getBarcode());
        order.setShipmentHandlerCode(handlerCode);
        order.setShipmentLabelUrl(response.getLabelUrl());
        order.setShipmentLabelGeneratedAt(LocalDateTime.now());

        return order;
    }
}
