package com.atamanahmet.vinylexchange.shipment.webhook;

import com.atamanahmet.vinylexchange.infrastructure.shipment.webhook.ShipmentWebhookHandler;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentWebhookHandlerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ShipmentWebhookHandler shipmentWebhookHandler;

    /** SHIPPED status routes to markShipped with shipment id, barcode, and handler shipment code */
    @Test
    void handleStatusChange_shipped_callsMarkShipped() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "shipment-123");
        payload.put("barcode", "BAR-456");
        payload.put("status", "SHIPPED");
        payload.put("handlerShipmentCode", "TRACK-789");

        shipmentWebhookHandler.handleStatusChange(payload);

        verify(orderService).markShipped("shipment-123", "BAR-456", "TRACK-789");
        verify(orderService, never()).markDelivered(any());
    }

    /** DELIVERED status routes to markDelivered with shipment order id */
    @Test
    void handleStatusChange_delivered_callsMarkDelivered() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "shipment-456");
        payload.put("barcode", "BAR-789");
        payload.put("status", "DELIVERED");

        shipmentWebhookHandler.handleStatusChange(payload);

        verify(orderService).markDelivered("shipment-456");
        verify(orderService, never()).markShipped(any(), any(), any());
    }

    /** Missing status key is ignored so no order state transition occurs */
    @Test
    void handleStatusChange_missingStatus_doesNothing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "shipment-123");
        payload.put("barcode", "BAR-456");

        shipmentWebhookHandler.handleStatusChange(payload);

        verify(orderService, never()).markShipped(any(), any(), any());
        verify(orderService, never()).markDelivered(any());
    }

    /** Unhandled status IN_TRANSIT is ignored without calling order service */
    @Test
    void handleStatusChange_unknownStatus_doesNothing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "shipment-123");
        payload.put("barcode", "BAR-456");
        payload.put("status", "IN_TRANSIT");

        shipmentWebhookHandler.handleStatusChange(payload);

        verify(orderService, never()).markShipped(any(), any(), any());
        verify(orderService, never()).markDelivered(any());
    }

    /** handlerShipmentCode nested under handler map is extracted for markShipped */
    @Test
    void handleStatusChange_handlerShipmentCodeInNestedHandler_extractsCorrectly() {
        Map<String, Object> handler = new HashMap<>();
        handler.put("handlerShipmentCode", "NESTED-TRACK");

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "shipment-123");
        payload.put("barcode", "BAR-456");
        payload.put("status", "SHIPPED");
        payload.put("handler", handler);

        shipmentWebhookHandler.handleStatusChange(payload);

        verify(orderService).markShipped("shipment-123", "BAR-456", "NESTED-TRACK");
    }

    /** Top-level handlerShipmentCode wins when both flat and nested values are present */
    @Test
    void handleStatusChange_bothFlatAndNestedHandlerCode_prefersCorrectOne() {
        Map<String, Object> handler = new HashMap<>();
        handler.put("handlerShipmentCode", "NESTED-TRACK");

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "shipment-123");
        payload.put("barcode", "BAR-456");
        payload.put("status", "SHIPPED");
        payload.put("handlerShipmentCode", "FLAT-TRACK");
        payload.put("handler", handler);

        shipmentWebhookHandler.handleStatusChange(payload);

        verify(orderService).markShipped("shipment-123", "BAR-456", "FLAT-TRACK");
    }

    /** Null id, barcode, and status must not throw or invoke order service */
    @Test
    void handleStatusChange_nullValues_doesNotThrow() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", null);
        payload.put("barcode", null);
        payload.put("status", null);

        assertThatCode(() -> shipmentWebhookHandler.handleStatusChange(payload))
                .doesNotThrowAnyException();

        verify(orderService, never()).markShipped(any(), any(), any());
        verify(orderService, never()).markDelivered(any());
    }
}
