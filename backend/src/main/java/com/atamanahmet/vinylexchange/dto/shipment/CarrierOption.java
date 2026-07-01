package com.atamanahmet.vinylexchange.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CarrierOption {

    private String code;
    private String name;
}
