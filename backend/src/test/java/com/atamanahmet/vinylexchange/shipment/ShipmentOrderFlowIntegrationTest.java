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
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import com.atamanahmet.vinylexchange.service.media.CloudinaryImageService;
import com.atamanahmet.vinylexchange.service.payment.PaymentService;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        private ShipmentService shipmentService;

        @Autowired
        private ShipmentWebhookHandler shipmentWebhookHandler;

        @Autowired
        private ObjectMapper objectMapper;

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
        void setUp() throws JsonProcessingException {
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
                                .orderNumber(99_999L)
                                .status(OrderStatus.PAID)
                                .saleType(SaleType.FIXED_PRICE)
                                .shippingAddressSnapshot(objectMapper.writeValueAsString(buyerSnapshot))
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
                lenient().when(userAddressService.serializeSnapshot(any(AddressSnapshot.class)))
                                .thenReturn(objectMapper.writeValueAsString(sellerSnapshot));
        }

        @AfterEach
        void tearDown() {
                if (order != null && order.getId() != null) {
                        orderRepository.deleteById(order.getId());
                }
        }

        /**
         * Full flow: label generated, webhook marks shipped, webhook marks delivered
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

                Map<String, Object> deliveredPayload = new HashMap<>();
                deliveredPayload.put("id", labeled.getShipmentOrderId());
                deliveredPayload.put("status", "DELIVERED");

                shipmentWebhookHandler.handleStatusChange(deliveredPayload);

                Order delivered = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
                assertThat(delivered.getDeliveredAt()).isNotNull();
        }

        /** Webhook endpoint accepts correct secret and processes status update */
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

                String url = "http://localhost:" + port + "/api/shipment/webhook/status?secret=test-shipment-webhook-secret";
                ResponseEntity<Void> response = restTemplate.postForEntity(url, payload, Void.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                Order shipped = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);
                assertThat(shipped.getShipmentTrackingNumber()).isEqualTo("TRACK-002");
        }

        /** Webhook endpoint rejects wrong secret and leaves order status unchanged */
        @Test
        void webhookEndpoint_wrongSecret_returnsUnauthorizedAndDoesNotUpdate() {
                UUID sellerAddressId = UUID.randomUUID();
                orderService.generateShipmentLabel(order.getId(), sellerUUID, "ARAS", sellerAddressId);

                Order labeled = orderService.generateShipmentLabel(order.getId(), sellerUUID, "ARAS", sellerAddressId);
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", labeled.getShipmentOrderId());
                payload.put("status", "SHIPPED");

                String url = "http://localhost:" + port + "/api/shipment/webhook/status?secret=wrong-secret";
                ResponseEntity<Void> response = restTemplate.postForEntity(url, payload, Void.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

                Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.PAID);
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
