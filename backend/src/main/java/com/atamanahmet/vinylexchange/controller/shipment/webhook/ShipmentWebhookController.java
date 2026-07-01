package com.atamanahmet.vinylexchange.controller.shipment.webhook;

import java.util.Map;

import com.atamanahmet.vinylexchange.infrastructure.shipment.webhook.ShipmentWebhookHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/shipment/webhook")
@RequiredArgsConstructor
public class ShipmentWebhookController {

    private final ShipmentWebhookHandler shipmentWebhookHandler;

    @Value("${shipment.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/status")
    public ResponseEntity<Void> handleStatus(
            @RequestParam("secret") String secret,
            @RequestBody Map<String, Object> payload) {

        if (!webhookSecret.equals(secret)) {
            log.warn("Shipment webhook rejected, invalid secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            shipmentWebhookHandler.handleStatusChange(payload);
        } catch (Exception ex) {
            log.error("Shipment webhook processing failed", ex);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}