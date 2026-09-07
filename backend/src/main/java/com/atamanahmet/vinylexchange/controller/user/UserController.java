package com.atamanahmet.vinylexchange.controller.user;

import java.util.List;

import com.atamanahmet.vinylexchange.session.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.service.payment.PaymentService;
import com.atamanahmet.vinylexchange.service.user.UserService;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.dto.payment.PaymentHistoryDto;
import com.atamanahmet.vinylexchange.dto.user.UpdateEmailRequest;
import com.atamanahmet.vinylexchange.dto.user.UserDTO;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final ListingService listingService;
    private final UserService userService;
    private final PaymentService paymentService;

    @GetMapping("/listings/active")
    public ResponseEntity<?> getMyActiveListings() {

        List<ListingDTO> listingDTOs = listingService.getUserListingsWithStatus(UserUtil.getCurrentUserId(),
                ListingStatus.AVAILABLE);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(listingDTOs);
    }

    @GetMapping("/listings/archived")
    public ResponseEntity<?> getMyArchivedListings() {

        List<ListingDTO> listingDTOs = listingService.getUserListingsWithStatus(UserUtil.getCurrentUserId(),
                ListingStatus.ARCHIVED);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(listingDTOs);
    }

    @GetMapping("/listings/sold")
    public ResponseEntity<?> getMySoldListings() {

        List<ListingDTO> listingDTOs = listingService.getUserListingsWithStatus(UserUtil.getCurrentUserId(),
                ListingStatus.SOLD);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(listingDTOs);
    }

    @PatchMapping("/email")
    public ResponseEntity<UserDTO> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        UserDTO updated = userService.updateEmail(UserUtil.getCurrentUserId(), request.email());
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentHistoryDto>> getMyPayments() {
        List<PaymentHistoryDto> payments =
                paymentService.listPaymentHistoryForBuyer(UserUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }
}
