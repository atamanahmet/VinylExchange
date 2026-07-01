package com.atamanahmet.vinylexchange.controller.shipment;

import java.util.List;

import com.atamanahmet.vinylexchange.infrastructure.shipment.ShipmentProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.shipment.CarrierOption;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentProvider shipmentProvider;

    @GetMapping("/carriers")
    public ResponseEntity<List<CarrierOption>> getAvailableCarriers() {
        return ResponseEntity.ok(shipmentProvider.getAvailableCarriers());
    }
}
