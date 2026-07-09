package com.atamanahmet.vinylexchange.shipment;

import com.atamanahmet.vinylexchange.service.shipment.ShipmentService;
import com.atamanahmet.vinylexchange.infrastructure.shipment.webhook.ShipmentWebhookHandler;
import com.atamanahmet.vinylexchange.config.BaseIntegrationTest;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.domain.enums.AddressType;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;
import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.atamanahmet.vinylexchange.exception.InvalidOrderOperationException;
import com.atamanahmet.vinylexchange.repository.order.OrderRepository;
import com.atamanahmet.vinylexchange.repository.order.OrderStatusHistoryRepository;
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import com.atamanahmet.vinylexchange.service.media.CloudinaryImageService;
import com.atamanahmet.vinylexchange.service.payment.PaymentService;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@TestPropertySource(properties = {
                "jwt.secret=test-jwt-secret-key-at-least-32-characters",
                "aes.encryption.key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                "spring.cloud.vault.enabled=false",
                "shipment.webhook.secret=test-shipment-webhook-secret"
})
class ShipmentOrderFlowIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private OrderService orderService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private OrderStatusHistoryRepository orderStatusHistoryRepository;

        @Autowired
        private ShipmentService shipmentService;

        @Autowired
        private ShipmentWebhookHandler shipmentWebhookHandler;

        @Autowired
        private TestRestTemplate restTemplate;

        @LocalServerPort
        private int port;

        @MockitoBean
        private UserAddressService userAddressService;

        @MockitoBean
        private CloudinaryImageService cloudinaryImageService;

        @MockitoBean
        private PaymentService paymentService;

        private UUID buyerUUID;
        private UUID sellerUUID;
        private Order order;
        private UserAddress sellerAddress;
        private AddressSnapshot buyerSnapshot;
        private AddressSnapshot sellerSnapshot;

        @BeforeEach
        void setUp() {
                buyerUUID = UUID.randomUUID();
                sellerUUID = UUID.randomUUID();

                buyerSnapshot = new AddressSnapshot(
                                "Buyer User",
                                "+905551112233",
                                "Buyer Street 10",
                                "Kadikoy",
                                "Istanbul",
                                "34000",
                                "TR");
                sellerSnapshot = new AddressSnapshot(
                                "Seller User",
                                "+905559998877",
                                "Seller Street 5",
                                "Cankaya",
                                "Ankara",
                                "06000",
                                "TR");

                order = Order.builder()
                                .buyerId(buyerUUID)
                                .sellerId(sellerUUID)
                                .orderNumber(ThreadLocalRandom.current().nextLong(1_000_000L, 9_999_999L))
                                .status(OrderStatus.PAID)
                                .saleType(SaleType.FIXED_PRICE)
                                .shippingAddressSnapshot(buyerSnapshot)
                                .build();
                order = orderRepository.save(order);

                sellerAddress = UserAddress.builder()
                                .id(UUID.randomUUID())
                                .userId(sellerUUID)
                                .label("Warehouse")
                                .fullName(sellerSnapshot.fullName())
                                .phone(sellerSnapshot.phone())
                                .addressLine(sellerSnapshot.addressLine())
                                .district(sellerSnapshot.district())
                                .city(sellerSnapshot.city())
                                .postalCode(sellerSnapshot.postalCode())
                                .country(sellerSnapshot.country())
                                .addressType(AddressType.SHIPPING)
                                .build();

                lenient().when(userAddressService.getAddressOrThrow(eq(sellerUUID), any(UUID.class)))
                                .thenReturn(sellerAddress);
                lenient().when(userAddressService.toSnapshot(any(UserAddress.class)))
                                .thenReturn(sellerSnapshot);
        }

        @AfterEach
        void tearDown() {
                if (order != null && order.getId() != null) {
                        orderStatusHistoryRepository.deleteAll(
                                        orderStatusHistoryRepository.findAllByOrderIdOrderByOccurredAtAsc(
                                                        order.getId()));
                        orderRepository.deleteById(order.getId());
                }
        }

        /**
         * Full flow: label generated, webhooks mark shipped, out for delivery, delivered
         */
        @Test
        void fullShipmentFlow_labelToDelivered_correctStatusTransitions() {
                UUID sellerAddressId = UUID.randomUUID();

                Order labeled = orderService.generateShipmentLabel(
                                order.getId(), sellerUUID, "ARAS", sellerAddressId);

                assertThat(labeled.getShipmentOrderId()).isNotNull().startsWith("MOCK-");
                assertThat(labeled.getShipmentBarcode()).isNotNull();
                assertThat(labeled.getShipmentHandlerCode()).isEqualTo("ARAS");
                assertThat(labeled.getShipmentLabelUrl()).isEqualTo("mock-label-url");
                assertThat(labeled.getStatus()).isEqualTo(OrderStatus.PAID);

                Map<String, Object> shippedPayload = new HashMap<>();
                shippedPayload.put("id", labeled.getShipmentOrderId());
                shippedPayload.put("barcode", labeled.getShipmentBarcode());
                shippedPayload.put("status", "SHIPPED");
                shippedPayload.put("handlerShipmentCode", "TRACK-001");

                shipmentWebhookHandler.handleStatusChange(shippedPayload);

                Order shipped = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);
                assertThat(shipped.getShipmentTrackingNumber()).isEqualTo("TRACK-001");
                assertThat(shipped.getAutoConfirmDeadline()).isNotNull();

                Map<String, Object> outForDeliveryPayload = new HashMap<>();
                outForDeliveryPayload.put("id", labeled.getShipmentOrderId());
                outForDeliveryPayload.put("status", "OUT_FOR_DELIVERY");

                shipmentWebhookHandler.handleStatusChange(outForDeliveryPayload);

                Order outForDelivery = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(outForDelivery.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);

                Map<String, Object> deliveredPayload = new HashMap<>();
                deliveredPayload.put("id", labeled.getShipmentOrderId());
                deliveredPayload.put("status", "DELIVERED");

                shipmentWebhookHandler.handleStatusChange(deliveredPayload);

                Order delivered = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
                assertThat(delivered.getDeliveredAt()).isNotNull();
        }

        /** Webhook endpoint accepts correct secret header and processes status update */
        @Test
        void webhookEndpoint_correctSecret_updatesOrderStatus() {
                UUID sellerAddressId = UUID.randomUUID();
                Order labeled = orderService.generateShipmentLabel(
                                order.getId(), sellerUUID, "ARAS", sellerAddressId);

                Map<String, Object> payload = new HashMap<>();
                payload.put("id", labeled.getShipmentOrderId());
                payload.put("barcode", labeled.getShipmentBarcode());
                payload.put("status", "SHIPPED");
                payload.put("handlerShipmentCode", "TRACK-002");

                String url = "http://localhost:" + port + "/api/shipment/webhook/status";
                ResponseEntity<Void> response = restTemplate.postForEntity(
                                url,
                                webhookRequest(payload, "test-shipment-webhook-secret"),
                                Void.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                Order shipped = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);
                assertThat(shipped.getShipmentTrackingNumber()).isEqualTo("TRACK-002");
        }

        /** Webhook endpoint rejects wrong secret header and leaves order status unchanged */
        @Test
        void webhookEndpoint_wrongSecret_returnsUnauthorizedAndDoesNotUpdate() {
                UUID sellerAddressId = UUID.randomUUID();
                Order labeled = orderService.generateShipmentLabel(
                                order.getId(), sellerUUID, "ARAS", sellerAddressId);

                Map<String, Object> payload = new HashMap<>();
                payload.put("id", labeled.getShipmentOrderId());
                payload.put("status", "SHIPPED");

                String url = "http://localhost:" + port + "/api/shipment/webhook/status";
                ResponseEntity<Void> response = restTemplate.postForEntity(
                                url,
                                webhookRequest(payload, "wrong-secret"),
                                Void.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

                Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        private HttpEntity<Map<String, Object>> webhookRequest(Map<String, Object> payload, String secret) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Webhook-Secret", secret);
                return new HttpEntity<>(payload, headers);
        }

        /** generateShipmentLabel with unknown orderId throws immediately */
        @Test
        void generateShipmentLabel_orderNotFound_throwsInvalidOperation() {
                assertThatThrownBy(() -> orderService.generateShipmentLabel(
                                UUID.randomUUID(), sellerUUID, "ARAS", UUID.randomUUID()))
                                .isInstanceOf(InvalidOrderOperationException.class);
        }

        /** Webhook with unknown shipmentOrderId throws immediately */
        @Test
        void markShipped_orderNotFound_throwsInvalidOperation() {
                assertThatThrownBy(() -> orderService.markShipped("NONEXISTENT-999", "barcode", "tracking"))
                                .isInstanceOf(InvalidOrderOperationException.class);
        }
}
