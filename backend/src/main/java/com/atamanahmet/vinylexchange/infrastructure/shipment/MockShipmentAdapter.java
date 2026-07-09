package com.atamanahmet.vinylexchange.infrastructure.shipment;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.atamanahmet.vinylexchange.dto.shipment.CarrierOption;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.dto.shipment.TrackingStatus;

import jakarta.annotation.PreDestroy;

/**
 * Demo adapter, active when shipment.provider=mock (default)
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "shipment.provider", havingValue = "mock", matchIfMissing = true)
public class MockShipmentAdapter implements ShipmentProvider {

    private final String appBaseUrl;
    private final String webhookSecret;
    private final RestTemplate restTemplate;
    private final ScheduledExecutorService executor;

    public MockShipmentAdapter(
            @Value("${app.base-url}") String appBaseUrl,
            @Value("${shipment.webhook.secret}") String webhookSecret,
            RestTemplate restTemplate) {
        this.appBaseUrl = appBaseUrl;
        this.webhookSecret = webhookSecret;
        this.restTemplate = restTemplate;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public CreateShipmentResponse createShipment(CreateShipmentRequest request) {
        String shipmentOrderId = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String barcode = String.format("%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L));

        log.info("[MOCK] Shipment created orderRef={} id={}", request.getOrderReference(), shipmentOrderId);

        CreateShipmentResponse response = CreateShipmentResponse.builder()
                .shipmentOrderId(shipmentOrderId)
                .barcode(barcode)
                .status("NEW")
                .labelUrl("mock-label-url")
                .build();

        String handlerCode = request.getHandlerCode();

        executor.schedule(() -> fireWebhook(shipmentOrderId, barcode, "SHIPPED", handlerCode), 30, TimeUnit.SECONDS);
        executor.schedule(() -> fireWebhook(shipmentOrderId, barcode, "OUT_FOR_DELIVERY", handlerCode), 60,
                TimeUnit.SECONDS);
        executor.schedule(() -> fireWebhook(shipmentOrderId, barcode, "DELIVERED", handlerCode), 90, TimeUnit.SECONDS);

        return response;
    }

    @Override
    public TrackingStatus getTrackingStatus(String shipmentOrderId) {
        log.info("[MOCK] Tracking status requested id={}", shipmentOrderId);
        return TrackingStatus.builder()
                .shipmentOrderId(shipmentOrderId)
                .status("SHIPPED")
                .handlerCode("MOCK")
                .build();
    }

    @Override
    public List<CarrierOption> getAvailableCarriers() {
        return List.of(
                CarrierOption.builder().code("ARAS").name("Aras Kargo").build(),
                CarrierOption.builder().code("YURTICI").name("Yurtici Kargo").build(),
                CarrierOption.builder().code("MNG").name("MNG Kargo").build(),
                CarrierOption.builder().code("PTT").name("PTT Kargo").build(),
                CarrierOption.builder().code("SURAT").name("Surat Kargo").build());
    }

    @PreDestroy
    void shutdownExecutor() {
        executor.shutdown();
    }

    private void fireWebhook(String shipmentOrderId, String barcode, String status, String handlerCode) {
        String handlerName = getAvailableCarriers().stream()
                .filter(carrier -> carrier.getCode().equals(handlerCode))
                .map(CarrierOption::getName)
                .findFirst()
                .orElse(handlerCode);

        WebhookPayload payload = new WebhookPayload(
                shipmentOrderId,
                barcode,
                status,
                new WebhookHandler(handlerName, handlerCode),
                barcode);

        log.info("Firing mock shipment webhook: orderId={}, status={}, handlerCode={}",
                shipmentOrderId, status, handlerCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Webhook-Secret", webhookSecret);

        restTemplate.postForObject(
                appBaseUrl + "/api/shipment/webhook/status",
                new HttpEntity<>(payload, headers),
                Void.class);
    }

    private record WebhookPayload(
            String id,
            String barcode,
            String status,
            WebhookHandler handler,
            String handlerShipmentCode) {
    }

    private record WebhookHandler(String name, String code) {
    }
}
