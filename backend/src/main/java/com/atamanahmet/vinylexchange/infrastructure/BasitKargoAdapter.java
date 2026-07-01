package com.atamanahmet.vinylexchange.infrastructure;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.atamanahmet.vinylexchange.infrastructure.shipment.ShipmentProvider;
import com.atamanahmet.vinylexchange.dto.shipment.CarrierOption;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.dto.shipment.TrackingStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Service
@Component
@ConditionalOnProperty(name = "shipment.provider", havingValue = "basit-kargo")
public class BasitKargoAdapter implements ShipmentProvider {

        private final RestTemplate restTemplate;
        private final String apiToken;
        private final String baseUrl;

        public BasitKargoAdapter(
                        RestTemplate restTemplate,
                        @Value("${basitkargo.api-token}") String apiToken,
                        @Value("${basitkargo.base-url:https://basitkargo.com/api}") String baseUrl) {
                this.restTemplate = restTemplate;
                this.apiToken = apiToken;
                this.baseUrl = baseUrl;
        }

        @Override
        public CreateShipmentResponse createShipment(CreateShipmentRequest request) {
                BasitKargoCreateOrderRequest body = new BasitKargoCreateOrderRequest(
                                request.getHandlerCode(),
                                "OUTGOING",
                                new BasitKargoContent(
                                                request.getOrderReference(),
                                                request.getOrderReference(),
                                                List.of(new BasitKargoPackage(
                                                                request.getPackageHeight(),
                                                                request.getPackageWidth(),
                                                                request.getPackageDepth(),
                                                                request.getPackageWeight()))),
                                new BasitKargoClient(
                                                request.getRecipientName(),
                                                request.getRecipientPhone(),
                                                request.getRecipientCity(),
                                                request.getRecipientDistrict(),
                                                request.getRecipientAddress()));

                BasitKargoOrderResponse response = restTemplate.postForObject(
                                baseUrl + "/v2/order/barcode",
                                new HttpEntity<>(body, authHeaders(MediaType.APPLICATION_JSON)),
                                BasitKargoOrderResponse.class);

                if (response == null || response.id() == null) {
                        throw new IllegalStateException("Empty or invalid response from BasitKargo create shipment");
                }

                return CreateShipmentResponse.builder()
                                .shipmentOrderId(response.id())
                                .barcode(response.barcode())
                                .status(response.status())
                                .labelUrl(baseUrl + "/label/svg/" + response.id())
                                .build();
        }

        @Override
        public TrackingStatus getTrackingStatus(String shipmentOrderId) {
                BasitKargoOrderResponse response = restTemplate.exchange(
                                baseUrl + "/v2/order/" + shipmentOrderId,
                                HttpMethod.GET,
                                new HttpEntity<>(authHeaders(null)),
                                BasitKargoOrderResponse.class).getBody();

                if (response == null || response.id() == null) {
                        throw new IllegalStateException("Empty or invalid response from BasitKargo tracking");
                }

                return TrackingStatus.builder()
                                .shipmentOrderId(response.id())
                                .barcode(response.barcode())
                                .status(response.status())
                                .handlerCode(response.handlerCode())
                                .handlerShipmentCode(response.handlerShipmentCode())
                                .build();
        }

        @Override
        public List<CarrierOption> getAvailableCarriers() {
                BasitKargoHandlerResponse[] handlers = restTemplate.exchange(
                                baseUrl + "/handlers",
                                HttpMethod.GET,
                                new HttpEntity<>(authHeaders(null)),
                                BasitKargoHandlerResponse[].class).getBody();

                if (handlers == null) {
                        return List.of();
                }

                return Arrays.stream(handlers)
                                .map(handler -> CarrierOption.builder()
                                                .code(handler.code())
                                                .name(handler.name())
                                                .build())
                                .toList();
        }

        private HttpHeaders authHeaders(MediaType contentType) {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(apiToken);
                if (contentType != null) {
                        headers.setContentType(contentType);
                }
                return headers;
        }

        private record BasitKargoCreateOrderRequest(
                        String handlerCode,
                        String type,
                        BasitKargoContent content,
                        BasitKargoClient client) {
        }

        private record BasitKargoContent(
                        String name,
                        String code,
                        List<BasitKargoPackage> packages) {
        }

        private record BasitKargoPackage(
                        int height,
                        int width,
                        int depth,
                        int weight) {
        }

        private record BasitKargoClient(
                        String name,
                        String phone,
                        String city,
                        String town,
                        String address) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record BasitKargoOrderResponse(
                        String id,
                        String barcode,
                        String status,
                        String handlerCode,
                        String handlerShipmentCode) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record BasitKargoHandlerResponse(String code, String name) {
        }
}
