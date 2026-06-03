package com.atamanahmet.vinylexchange.controller.user;

import java.util.List;

import com.atamanahmet.vinylexchange.session.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final ListingService listingService;

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
}
