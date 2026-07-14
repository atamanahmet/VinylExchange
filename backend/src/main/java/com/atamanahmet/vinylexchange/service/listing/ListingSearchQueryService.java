package com.atamanahmet.vinylexchange.service.listing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.dto.listing.ListingSummaryResponse;
import com.atamanahmet.vinylexchange.infrastructure.search.service.SearchPort;
import com.atamanahmet.vinylexchange.mapper.ListingMapper;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingSearchQueryService {

        private final SearchPort searchPort;
        private final ListingService listingService;
        private final ListingMapper listingMapper;

        @Transactional(readOnly = true)
        public Page<ListingSummaryResponse> search(String query, Pageable pageable) {
                Page<UUID> idPage = searchPort.searchIds(query, pageable);

                if (idPage.isEmpty()) {
                        return Page.empty(idPage.getPageable());
                }

                List<UUID> orderedIds = idPage.getContent();
                Map<UUID, Listing> byId = listingService.getListingsByIds(orderedIds).stream()
                                .collect(Collectors.toMap(Listing::getId, Function.identity(), (a, b) -> a));

                List<Listing> orderedListings = orderListings(orderedIds, byId, pageable.getSort());

                List<ListingSummaryResponse> orderedDtos = orderedListings.stream()
                                .map(listingMapper::toSummaryDto)
                                .map(listingMapper::toResponse)
                                .toList();

                return new PageImpl<>(orderedDtos, pageable, idPage.getTotalElements());
        }

        /**
         * FTS/OpenSearch order when unsorted; otherwise apply Pageable sort on hydrated entities.
         * Search adapters ignore Sort — sorting happens here after ID fetch.
         */
        private static List<Listing> orderListings(
                        List<UUID> relevanceOrder,
                        Map<UUID, Listing> byId,
                        Sort sort) {
                if (sort == null || sort.isUnsorted()) {
                        return relevanceOrder.stream()
                                        .map(byId::get)
                                        .filter(Objects::nonNull)
                                        .toList();
                }

                List<Listing> listings = new ArrayList<>(byId.values());
                listings.sort(listingComparator(sort));
                return listings;
        }

        private static Comparator<Listing> listingComparator(Sort sort) {
                Comparator<Listing> comparator = null;
                for (Sort.Order order : sort) {
                        Comparator<Listing> next = comparatorFor(order);
                        comparator = comparator == null ? next : comparator.thenComparing(next);
                }
                return comparator != null ? comparator : (a, b) -> 0;
        }

        private static Comparator<Listing> comparatorFor(Sort.Order order) {
                Comparator<Listing> byProperty = switch (order.getProperty()) {
                        case "priceKurus" -> Comparator.comparingLong(Listing::getPriceKurus);
                        case "year" -> Comparator.comparingInt(Listing::getYear);
                        case "createdAt" -> Comparator.comparing(
                                        Listing::getCreatedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder()));
                        case "title" -> Comparator.comparing(
                                        Listing::getTitle,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                        default -> (a, b) -> 0;
                };
                return order.isDescending() ? byProperty.reversed() : byProperty;
        }
}
