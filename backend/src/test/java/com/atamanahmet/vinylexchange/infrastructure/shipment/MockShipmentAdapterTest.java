package com.atamanahmet.vinylexchange.infrastructure.shipment;

import com.atamanahmet.vinylexchange.dto.shipment.CarrierOption;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.dto.shipment.TrackingStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MockShipmentAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private MockShipmentAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockShipmentAdapter("http://localhost:8080", restTemplate);
    }

    @AfterEach
    void tearDown() {
        adapter.shutdownExecutor();
    }

    private CreateShipmentRequest buildRequest() {
        return CreateShipmentRequest.builder()
                .handlerCode("ARAS")
                .orderReference("1001")
                .senderName("Seller User")
                .senderPhone("+905559998877")
                .senderCity("Ankara")
                .senderDistrict("Cankaya")
                .senderAddress("Seller Street 5")
                .recipientName("Buyer User")
                .recipientPhone("+905551112233")
                .recipientCity("Istanbul")
                .recipientDistrict("Kadikoy")
                .recipientAddress("Buyer Street 10")
                .packageHeight(10)
                .packageWidth(15)
                .packageDepth(5)
                .packageWeight(1)
                .build();
    }

    /** Created shipment has NEW status and MOCK prefixed shipmentOrderId */
    @Test
    void createShipment_returnsNewStatusWithMockId() {
        CreateShipmentResponse response = adapter.createShipment(buildRequest());

        assertThat(response.getStatus()).isEqualTo("NEW");
        assertThat(response.getShipmentOrderId()).startsWith("MOCK-");
        // MockShipmentAdapter formats barcodes as zero-padded 10-digit strings (%010d).
        assertThat(response.getBarcode()).isNotNull().hasSize(10);
        assertThat(response.getLabelUrl()).isEqualTo("mock-label-url");
    }

    /** Two calls must produce different shipmentOrderIds and barcodes */
    @Test
    void createShipment_eachCallReturnsUniqueIds() {
        CreateShipmentResponse first = adapter.createShipment(buildRequest());
        CreateShipmentResponse second = adapter.createShipment(buildRequest());

        assertThat(first.getShipmentOrderId()).isNotEqualTo(second.getShipmentOrderId());
        assertThat(first.getBarcode()).isNotEqualTo(second.getBarcode());
    }

    /** Mock always returns SHIPPED for any shipmentOrderId */
    @Test
    void getTrackingStatus_alwaysReturnsShipped() {
        TrackingStatus status = adapter.getTrackingStatus("any-id");

        assertThat(status.getStatus()).isEqualTo("SHIPPED");
        assertThat(status.getHandlerCode()).isEqualTo("MOCK");
    }

    /** Mock returns the five Turkish shipment companies */
    @Test
    void getAvailableCarriers_returnsFiveCarriers() {
        List<CarrierOption> carriers = adapter.getAvailableCarriers();

        assertThat(carriers).hasSize(5);
        assertThat(carriers)
                .extracting(CarrierOption::getCode)
                .containsExactlyInAnyOrder("ARAS", "YURTICI", "MNG", "PTT", "SURAT");
    }
}
