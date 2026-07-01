package com.atamanahmet.vinylexchange.shipment.provider;

import com.atamanahmet.vinylexchange.infrastructure.BasitKargoAdapter;
import com.atamanahmet.vinylexchange.dto.shipment.CarrierOption;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.dto.shipment.TrackingStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class BasitKargoAdapterTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private BasitKargoAdapter adapter;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        adapter = new BasitKargoAdapter(restTemplate, "test-token", "http://mock-api");
    }

    @AfterEach
    void tearDown() {
        mockServer.verify();
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

    /** POST /v2/order/barcode response maps id, barcode, status and builds label URL */
    @Test
    void createShipment_successResponse_returnsMappedResponse() {
        mockServer.expect(requestTo("http://mock-api/v2/order/barcode"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "id": "order-abc",
                          "barcode": "1234567890",
                          "status": "NEW"
                        }
                        """, MediaType.APPLICATION_JSON));

        CreateShipmentResponse response = adapter.createShipment(buildRequest());

        assertThat(response.getShipmentOrderId()).isEqualTo("order-abc");
        assertThat(response.getBarcode()).isEqualTo("1234567890");
        assertThat(response.getStatus()).isEqualTo("NEW");
        assertThat(response.getLabelUrl()).contains("order-abc");
    }

    /** Empty create-shipment body must fail fast with clear error */
    @Test
    void createShipment_nullResponse_throwsIllegalState() {
        mockServer.expect(requestTo("http://mock-api/v2/order/barcode"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.createShipment(buildRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Empty or invalid");
    }

    /** GET /v2/order/{id} response maps tracking fields from provider payload */
    @Test
    void getTrackingStatus_successResponse_returnsMappedStatus() {
        mockServer.expect(requestTo("http://mock-api/v2/order/order-123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": "order-123",
                          "barcode": "BAR-999",
                          "status": "SHIPPED",
                          "handlerCode": "ARAS",
                          "handlerShipmentCode": "TRACK-001"
                        }
                        """, MediaType.APPLICATION_JSON));

        TrackingStatus status = adapter.getTrackingStatus("order-123");

        assertThat(status.getShipmentOrderId()).isEqualTo("order-123");
        assertThat(status.getBarcode()).isEqualTo("BAR-999");
        assertThat(status.getStatus()).isEqualTo("SHIPPED");
        assertThat(status.getHandlerCode()).isEqualTo("ARAS");
        assertThat(status.getHandlerShipmentCode()).isEqualTo("TRACK-001");
    }

    /** Empty tracking body must fail fast with clear error */
    @Test
    void getTrackingStatus_nullResponse_throwsIllegalState() {
        mockServer.expect(requestTo("http://mock-api/v2/order/order-123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.getTrackingStatus("order-123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Empty or invalid");
    }

    /** GET /handlers maps carrier code and name for each handler in array */
    @Test
    void getAvailableCarriers_successResponse_returnsList() {
        mockServer.expect(requestTo("http://mock-api/handlers"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {"code": "ARAS", "name": "Aras Kargo"},
                          {"code": "MNG", "name": "MNG Kargo"}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<CarrierOption> carriers = adapter.getAvailableCarriers();

        assertThat(carriers).hasSize(2);
        assertThat(carriers.get(0).getCode()).isEqualTo("ARAS");
        assertThat(carriers.get(0).getName()).isEqualTo("Aras Kargo");
        assertThat(carriers.get(1).getCode()).isEqualTo("MNG");
        assertThat(carriers.get(1).getName()).isEqualTo("MNG Kargo");
    }

    /** Null handlers body degrades to empty list without throwing */
    @Test
    void getAvailableCarriers_nullResponse_returnsEmptyList() {
        mockServer.expect(requestTo("http://mock-api/handlers"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withNoContent());

        List<CarrierOption> carriers = adapter.getAvailableCarriers();

        assertThat(carriers).isEmpty();
    }
}
