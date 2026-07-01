package com.atamanahmet.vinylexchange.mapper;

import java.util.List;

import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.dto.address.AddressRequest;
import com.atamanahmet.vinylexchange.dto.address.AddressResponse;

public class AddressMapper {

    private AddressMapper() {}

    public static UserAddress toEntity(AddressRequest request) {
        return UserAddress.builder()
                .label(request.getLabel())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .district(request.getDistrict())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(resolveCountry(request.getCountry()))
                .addressType(request.getAddressType())
                .isDefault(resolveIsDefault(request.getIsDefault()))
                .build();
    }

    public static AddressResponse toResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .district(address.getDistrict())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    public static List<AddressResponse> toResponseList(List<UserAddress> addresses) {
        return addresses.stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    public static void applyUpdate(UserAddress existing, AddressRequest request) {
        if (request.getLabel() != null) {
            existing.setLabel(request.getLabel());
        }
        if (request.getFullName() != null) {
            existing.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone());
        }
        if (request.getAddressLine() != null) {
            existing.setAddressLine(request.getAddressLine());
        }
        if (request.getDistrict() != null) {
            existing.setDistrict(request.getDistrict());
        }
        if (request.getCity() != null) {
            existing.setCity(request.getCity());
        }
        if (request.getPostalCode() != null) {
            existing.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            existing.setCountry(resolveCountry(request.getCountry()));
        }
        if (request.getAddressType() != null) {
            existing.setAddressType(request.getAddressType());
        }
        if (request.getIsDefault() != null) {
            existing.setDefault(request.getIsDefault());
        }
    }

    private static String resolveCountry(String country) {
        if (country == null || country.isBlank()) {
            return "TR";
        }
        return country;
    }

    private static boolean resolveIsDefault(Boolean isDefault) {
        return isDefault != null && isDefault;
    }
}
