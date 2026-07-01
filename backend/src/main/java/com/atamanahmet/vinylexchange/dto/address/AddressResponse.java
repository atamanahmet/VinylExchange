package com.atamanahmet.vinylexchange.dto.address;

import java.time.LocalDateTime;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.AddressType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private UUID id;
    private String label;
    private String fullName;
    private String phone;
    private String addressLine;
    private String district;
    private String city;
    private String postalCode;
    private String country;
    private AddressType addressType;

    @JsonProperty("isDefault")
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
