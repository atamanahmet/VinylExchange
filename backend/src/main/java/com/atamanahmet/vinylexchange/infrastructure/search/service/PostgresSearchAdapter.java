package com.atamanahmet.vinylexchange.infrastructure.search.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostgresSearchAdapter implements SearchPort {

    private final Logger log = LoggerFactory.getLogger(PostgresSearchAdapter.class);
    private final ListingRepository listingRepository;

    @Override
    public Page<UUID> searchIds(String query, int page, int size) {
        try {
            int offset = page * size;

            if (query == null || query.isBlank()) {
                List<UUID> ids = listingRepository.findAllAvailableIds(size, offset);
                long total = listingRepository.countByStatusAndStockQuantityGreaterThanAndOnHoldFalse(
                        ListingStatus.AVAILABLE, 0);
                return new PageImpl<>(ids, PageRequest.of(page, size), total);
            }

            List<UUID> ids = listingRepository.fullTextSearch(query, size, offset);
            long total = listingRepository.countFullTextSearch(query);

            return new PageImpl<>(ids, PageRequest.of(page, size), total);

        } catch (Exception e) {
            log.error("Postgres search failed", e);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        }
    }
}