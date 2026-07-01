package com.atamanahmet.vinylexchange.shipment.webhook;

import java.util.Map;

import com.atamanahmet.vinylexchange.controller.shipment.webhook.ShipmentWebhookController;
import com.atamanahmet.vinylexchange.infrastructure.shipment.webhook.ShipmentWebhookHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentWebhookControllerTest {

    @Mock
    private ShipmentWebhookHandler shipmentWebhookHandler;

    private ShipmentWebhookController controller;

    private static final String SECRET = "test-secret";

    private void initController() {
        controller = new ShipmentWebhookController(shipmentWebhookHandler);
        ReflectionTestUtils.setField(controller, "webhookSecret", SECRET);
    }

    /** Correct secret delegates payload to handler and returns 200 */
    @Test
    void handleStatus_correctSecret_processesPayloadAndReturnsOk() {
        initController();
        Map<String, Object> payload = Map.of("id", "shipment-123", "status", "SHIPPED");

        ResponseEntity<Void> response = controller.handleStatus(SECRET, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(shipmentWebhookHandler).handleStatusChange(payload);
    }

    /** Wrong secret is rejected with 401, handler never called */
    @Test
    void handleStatus_wrongSecret_returnsUnauthorized() {
        initController();
        Map<String, Object> payload = Map.of("id", "shipment-123", "status", "SHIPPED");

        ResponseEntity<Void> response = controller.handleStatus("wrong-secret", payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(shipmentWebhookHandler, never()).handleStatusChange(payload);
    }

    @Test
    void handleStatus_handlerThrows_returnsInternalServerError() {
        initController();
        Map<String, Object> payload = Map.of("id", "shipment-123", "status", "SHIPPED");
        doThrow(new RuntimeException("boom")).when(shipmentWebhookHandler).handleStatusChange(payload);

        ResponseEntity<Void> response = controller.handleStatus(SECRET, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}