package com.atamanahmet.vinylexchange.dto.order;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckOutresultDTO {

    private boolean success;
    private String message;
    private UUID orderId;
}
