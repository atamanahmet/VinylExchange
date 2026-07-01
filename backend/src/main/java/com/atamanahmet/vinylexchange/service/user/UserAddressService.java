package com.atamanahmet.vinylexchange.service.user;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.domain.enums.AddressType;
import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.atamanahmet.vinylexchange.dto.address.AddressRequest;
import com.atamanahmet.vinylexchange.dto.address.AddressResponse;
import com.atamanahmet.vinylexchange.exception.ResourceNotFoundException;
import com.atamanahmet.vinylexchange.mapper.AddressMapper;
import com.atamanahmet.vinylexchange.repository.UserAddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final ObjectMapper objectMapper;

    /** Returns all addresses for a user filtered by type. */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByType(UUID userId, AddressType type) {
        return AddressMapper.toResponseList(userAddressRepository.findByUserIdAndAddressType(userId, type));
    }

    /** Returns all addresses for a user. */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses(UUID userId) {
        return AddressMapper.toResponseList(userAddressRepository.findByUserId(userId));
    }

    /**
     * Creates a new address. If it is marked default, clears default flag on all
     * other addresses of same type for this user first.
     */
    @Transactional
    public AddressResponse createAddress(UUID userId, AddressRequest request) {
        UserAddress address = AddressMapper.toEntity(request);
        address.setUserId(userId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultFlags(userId, address.getAddressType(), null);
        }

        UserAddress saved = userAddressRepository.save(address);
        return AddressMapper.toResponse(saved);
    }

    /** Updates existing address. Validates ownership. */
    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        UserAddress existing = getAddressOrThrow(userId, addressId);

        AddressMapper.applyUpdate(existing, request);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultFlags(userId, existing.getAddressType(), addressId);
        }

        UserAddress saved = userAddressRepository.save(existing);
        return AddressMapper.toResponse(saved);
    }

    /** Deletes address. Validates ownership. */
    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = getAddressOrThrow(userId, addressId);
        userAddressRepository.delete(address);
    }

    /** Returns true if user has at least one SHIPPING address. Used by listing creation guard. */
    @Transactional(readOnly = true)
    public boolean hasShippingAddress(UUID userId) {
        return !userAddressRepository.findByUserIdAndAddressType(userId, AddressType.SHIPPING).isEmpty();
    }

    /**
     * Fetches address by id, validates it belongs to userId, throws if not found or
     * not owned.
     */
    @Transactional(readOnly = true)
    public UserAddress getAddressOrThrow(UUID userId, UUID addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        if (!address.getUserId().equals(userId)) {
            throw new AccessDeniedException("Address does not belong to user");
        }

        return address;
    }

    /** Converts UserAddress to AddressSnapshot. */
    public AddressSnapshot toSnapshot(UserAddress address) {
        return new AddressSnapshot(
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getDistrict(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry());
    }

    /**
     * Serializes AddressSnapshot to JSON string. Returns encrypted-ready string via
     * ObjectMapper.
     */
    public String serializeSnapshot(AddressSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize address snapshot", e);
        }
    }

    private void clearDefaultFlags(UUID userId, AddressType addressType, UUID exceptAddressId) {
        userAddressRepository.findByUserIdAndAddressType(userId, addressType).stream()
                .filter(address -> address.isDefault())
                .filter(address -> exceptAddressId == null || !address.getId().equals(exceptAddressId))
                .forEach(address -> {
                    address.setDefault(false);
                    userAddressRepository.save(address);
                });
    }
}
