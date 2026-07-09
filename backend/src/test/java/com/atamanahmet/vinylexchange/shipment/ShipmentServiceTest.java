package com.atamanahmet.vinylexchange.shipment;

import com.atamanahmet.vinylexchange.infrastructure.shipment.ShipmentProvider;
import com.atamanahmet.vinylexchange.service.shipment.ShipmentService;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.domain.enums.AddressType;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentProvider shipmentProvider;

    @Mock
    private UserAddressService userAddressService;

    private ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService(shipmentProvider, userAddressService);
    }

    private AddressSnapshot buildBuyerSnapshot() {
        return new AddressSnapshot(
                "Buyer User",
                "+905551112233",
                "Buyer Street 10",
                "Kadikoy",
                "Istanbul",
                "34000",
                "TR");
    }

    private Order buildOrder() {
        return Order.builder()
                .id(UUID.randomUUID())
                .orderNumber(1001L)
                .status(OrderStatus.PAID)
                .shippingAddressSnapshot(buildBuyerSnapshot())
                .build();
    }

    private UserAddress buildSellerAddress() {
        return UserAddress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .label("Home")
                .fullName("Seller User")
                .phone("+905559998877")
                .addressLine("Seller Street 5")
                .district("Cankaya")
                .city("Ankara")
                .postalCode("06000")
                .country("TR")
                .addressType(AddressType.SHIPPING)
                .build();
    }

    private AddressSnapshot buildSellerSnapshot() {
        return new AddressSnapshot(
                "Seller User",
                "+905559998877",
                "Seller Street 5",
                "Cankaya",
                "Ankara",
                "06000",
                "TR");
    }

    private void setupAddressMocks(AddressSnapshot seller) {
        when(userAddressService.toSnapshot(any(UserAddress.class))).thenReturn(seller);
    }

    /** Successful shipment creation writes all shipment fields from provider response onto order */
    @Test
    void createShipmentForOrder_validInputs_setsShipmentFieldsOnOrder() throws Exception {
        Order order = buildOrder();
        UserAddress sellerAddress = buildSellerAddress();
        AddressSnapshot sellerSnapshot = buildSellerSnapshot();

        setupAddressMocks(sellerSnapshot);

        CreateShipmentResponse response = CreateShipmentResponse.builder()
                .shipmentOrderId("shipment-order-1")
                .barcode("BAR-001")
                .status("CREATED")
                .labelUrl("https://example.com/label.svg")
                .build();
        when(shipmentProvider.createShipment(any(CreateShipmentRequest.class))).thenReturn(response);

        Order result = shipmentService.createShipmentForOrder(order, "ARAS", sellerAddress);

        assertThat(result.getShipmentOrderId()).isEqualTo("shipment-order-1");
        assertThat(result.getShipmentBarcode()).isEqualTo("BAR-001");
        assertThat(result.getShipmentHandlerCode()).isEqualTo("ARAS");
        assertThat(result.getShipmentLabelUrl()).isEqualTo("https://example.com/label.svg");
        assertThat(result.getShipmentLabelGeneratedAt()).isNotNull();
    }

    /** Null shipping snapshot cannot build shipment request */
    @Test
    void createShipmentForOrder_nullSnapshot_throwsIllegalState() {
        Order order = buildOrder();
        order.setShippingAddressSnapshot(null);

        assertThatThrownBy(() -> shipmentService.createShipmentForOrder(order, "ARAS", buildSellerAddress()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing");
    }

    /** Provider failure propagates without mutating order shipment fields */
    @Test
    void createShipmentForOrder_providerThrows_exceptionPropagates() throws Exception {
        Order order = buildOrder();
        UserAddress sellerAddress = buildSellerAddress();
        AddressSnapshot sellerSnapshot = buildSellerSnapshot();

        setupAddressMocks(sellerSnapshot);
        when(shipmentProvider.createShipment(any(CreateShipmentRequest.class)))
                .thenThrow(new IllegalStateException("provider failed"));

        assertThatThrownBy(() -> shipmentService.createShipmentForOrder(order, "ARAS", sellerAddress))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("provider failed");
    }

    /** CreateShipmentRequest maps handler code and address cities from buyer and seller snapshots */
    @Test
    void createShipmentForOrder_requestBuiltCorrectly() throws Exception {
        Order order = buildOrder();
        UserAddress sellerAddress = buildSellerAddress();
        AddressSnapshot sellerSnapshot = buildSellerSnapshot();

        setupAddressMocks(sellerSnapshot);
        when(shipmentProvider.createShipment(any(CreateShipmentRequest.class)))
                .thenReturn(CreateShipmentResponse.builder()
                        .shipmentOrderId("shipment-order-1")
                        .barcode("BAR-001")
                        .status("CREATED")
                        .labelUrl("https://example.com/label.svg")
                        .build());

        shipmentService.createShipmentForOrder(order, "ARAS", sellerAddress);

        ArgumentCaptor<CreateShipmentRequest> requestCaptor = ArgumentCaptor.forClass(CreateShipmentRequest.class);
        verify(shipmentProvider).createShipment(requestCaptor.capture());

        CreateShipmentRequest request = requestCaptor.getValue();
        assertThat(request.getHandlerCode()).isEqualTo("ARAS");
        assertThat(request.getOrderReference()).isEqualTo("1001");
        assertThat(request.getRecipientCity()).isEqualTo("Istanbul");
        assertThat(request.getSenderCity()).isEqualTo("Ankara");
    }
}
