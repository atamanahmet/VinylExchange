package com.atamanahmet.vinylexchange.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentResponse {

    private String shipmentOrderId;
    private String barcode;
    private String status;
    private String labelUrl;
}
