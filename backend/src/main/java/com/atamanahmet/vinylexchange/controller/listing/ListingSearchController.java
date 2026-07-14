package com.atamanahmet.vinylexchange.controller.listing;

import com.atamanahmet.vinylexchange.service.listing.ListingSearchQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.listing.ListingSummaryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings/search")
public class ListingSearchController {

    private final ListingSearchQueryService listingSearchQueryService;

    @GetMapping
    public ResponseEntity<Page<ListingSummaryResponse>> search(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ListingSummaryResponse> searchResult = listingSearchQueryService.search(query, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchResult);
    }
}
