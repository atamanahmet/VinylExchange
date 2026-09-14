package com.atamanahmet.vinylexchange.service.listing;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.dto.listing.ListingSummaryDto;
import com.atamanahmet.vinylexchange.mapper.ListingMapper;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListingCacheStore {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    /**
     * Fetches up to 60 available listings for given sort, result stored in Redis
     */
    @Cacheable(value = "listings", key = "#pageable.sort.toString()", cacheManager = "readCacheManager")
    public List<ListingSummaryDto> getTop60ForSort(Pageable pageable) {
        Pageable maxPage = PageRequest.of(0, 60, pageable.getSort());
        return listingRepository.findAllWithStatus(ListingStatus.AVAILABLE, maxPage)
                .map(listingMapper::toSummaryDto)
                .getContent();
    }

    /**
     * True number of browsable listings. The cached slice above holds at most 60,
     * so it cannot be used as a page total. Lives in the same "listings" cache so
     * the existing allEntries CacheEvict calls clear it too.
     */
    @Cacheable(value = "listings", key = "'availableCount'", cacheManager = "readCacheManager")
    public long countAvailable() {
        return listingRepository.countByStatusAndStockQuantityGreaterThanAndOnHoldFalse(
                ListingStatus.AVAILABLE, 0);
    }
}
