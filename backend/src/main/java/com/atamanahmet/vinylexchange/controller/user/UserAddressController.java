package com.atamanahmet.vinylexchange.controller.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.domain.enums.AddressType;
import com.atamanahmet.vinylexchange.dto.address.AddressRequest;
import com.atamanahmet.vinylexchange.dto.address.AddressResponse;
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import com.atamanahmet.vinylexchange.session.UserUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @RequestParam Optional<AddressType> type) {

        UUID userId = UserUtil.getCurrentUserId();

        if (type.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userAddressService.getAddressesByType(userId, type.get()));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userAddressService.getAllAddresses(userId));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@RequestBody @Valid AddressRequest request) {

        UUID userId = UserUtil.getCurrentUserId();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userAddressService.createAddress(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable UUID id,
            @RequestBody @Valid AddressRequest request) {

        UUID userId = UserUtil.getCurrentUserId();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userAddressService.updateAddress(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id) {

        UUID userId = UserUtil.getCurrentUserId();

        userAddressService.deleteAddress(userId, id);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
