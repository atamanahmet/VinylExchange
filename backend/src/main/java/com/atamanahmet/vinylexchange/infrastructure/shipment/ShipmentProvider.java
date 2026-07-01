package com.atamanahmet.vinylexchange.infrastructure.shipment;

import java.util.List;

import com.atamanahmet.vinylexchange.dto.shipment.CarrierOption;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentRequest;
import com.atamanahmet.vinylexchange.dto.shipment.CreateShipmentResponse;
import com.atamanahmet.vinylexchange.dto.shipment.TrackingStatus;

public interface ShipmentProvider {

    CreateShipmentResponse createShipment(CreateShipmentRequest request);

    TrackingStatus getTrackingStatus(String shipmentOrderId);

    List<CarrierOption> getAvailableCarriers();
}
