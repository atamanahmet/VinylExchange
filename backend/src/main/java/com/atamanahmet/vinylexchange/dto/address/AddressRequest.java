package com.atamanahmet.vinylexchange.dto.address;

import com.atamanahmet.vinylexchange.domain.enums.AddressType;

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
public class AddressRequest {

    @NotBlank
    private String label;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    @NotBlank
    private String addressLine;

    @NotBlank
    private String district;

    @NotBlank
    private String city;

    @NotBlank
    private String postalCode;

    private String country;

    @NotNull
    private AddressType addressType;

    private Boolean isDefault;
}
