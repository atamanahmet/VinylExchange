package com.atamanahmet.vinylexchange.infrastructure.shipment.webhook;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.service.order.OrderService;

@Service
public class ShipmentWebhookHandler {

    private final OrderService orderService;
    private final Logger logger;

    public ShipmentWebhookHandler(OrderService orderService) {
        this.orderService = orderService;
        this.logger = LoggerFactory.getLogger(ShipmentWebhookHandler.class);
    }

    /** Processes status change webhook from shipment provider. Updates order state. */
    public void handleStatusChange(Map<String, Object> payload) {
        logger.info("Incoming shipment status webhook: {}", payload);

        String shipmentOrderId = stringValue(payload.get("id"));
        String barcode = stringValue(payload.get("barcode"));
        String status = stringValue(payload.get("status"));
        String handlerShipmentCode = extractHandlerShipmentCode(payload);

        if (status == null) {
            logger.debug("Ignoring shipment webhook with missing status");
            return;
        }

        switch (status) {
            case "SHIPPED" -> orderService.markShipped(shipmentOrderId, barcode, handlerShipmentCode);
            case "OUT_FOR_DELIVERY" -> orderService.markOutForDelivery(shipmentOrderId);
            case "DELIVERED" -> orderService.markDelivered(shipmentOrderId);
            case "RETURNING", "RETURNED" -> logger.warn(
                    "Return flow not implemented for shipment order {} status {}", shipmentOrderId, status);
            case "LOST", "NEEDS_SUPPORT" -> logger.error(
                    "Shipment order {} requires attention, status {}", shipmentOrderId, status);
            default -> logger.debug("Ignoring unhandled shipment status {} for order {}", status, shipmentOrderId);
        }
    }

    private String extractHandlerShipmentCode(Map<String, Object> payload) {
        Object topLevel = payload.get("handlerShipmentCode");
        if (topLevel != null) {
            return topLevel.toString();
        }

        Object handler = payload.get("handler");
        if (handler instanceof Map<?, ?> handlerMap) {
            Object shipmentCode = handlerMap.get("handlerShipmentCode");
            if (shipmentCode != null) {
                return shipmentCode.toString();
            }
        }

        return null;
    }

    private String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
