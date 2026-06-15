package com.atamanahmet.vinylexchange.service.listing;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.listing.ListingDTO;
import com.atamanahmet.vinylexchange.infrastructure.search.service.SearchPort;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingSearchQueryService {

        private final SearchPort searchPort;
        private final ListingService listingService;

        public Page<ListingDTO> search(String query, int page, int size) {
                Page<UUID> idPage = searchPort.searchIds(query, page, size);

                if (idPage.isEmpty()) {
                        return Page.empty(idPage.getPageable());
                }

                List<UUID> orderedIds = idPage.getContent();
                List<ListingDTO> unorderedDtos = listingService.getListingDTOsWithIds(orderedIds);

                Map<UUID, ListingDTO> byId = unorderedDtos.stream()
                        .collect(Collectors.toMap(ListingDTO::getId, dto -> dto, (a, b) -> a));

                List<ListingDTO> orderedDtos = orderedIds.stream()
                        .map(byId::get)
                        .filter(Objects::nonNull)
                        .toList();

                return new PageImpl<>(orderedDtos, PageRequest.of(page, size), idPage.getTotalElements());
        }
}