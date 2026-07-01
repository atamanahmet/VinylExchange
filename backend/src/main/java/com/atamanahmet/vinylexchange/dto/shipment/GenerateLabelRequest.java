package com.atamanahmet.vinylexchange.dto.shipment;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateLabelRequest {

    @NotBlank
    private String handlerCode;

    @NotNull
    private UUID sellerAddressId;
}
