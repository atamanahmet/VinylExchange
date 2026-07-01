package com.atamanahmet.vinylexchange.domain.snapshot;

public record AddressSnapshot(
        String fullName,
        String phone,
        String addressLine,
        String district,
        String city,
        String postalCode,
        String country) {
}
