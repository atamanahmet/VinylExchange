package com.atamanahmet.vinylexchange.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    private String handlerCode;
    private String orderReference;
    private String senderName;
    private String senderPhone;
    private String senderCity;
    private String senderDistrict;
    private String senderAddress;
    private String recipientName;
    private String recipientPhone;
    private String recipientCity;
    private String recipientDistrict;
    private String recipientAddress;
    private int packageHeight;
    private int packageWidth;
    private int packageDepth;
    private int packageWeight;
}
