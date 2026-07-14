package com.atamanahmet.vinylexchange.infrastructure.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Decides which search adapter to use at runtime.
 * OpenSearch when healthy, Postgres otherwise.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class SearchRouter implements SearchPort {

    private final PostgresSearchAdapter postgresSearchAdapter;
    private final OpenSearchAdapter openSearchAdapter;
    private final SearchHealthIndicator searchHealthIndicator;

    @Override
    public Page<UUID> searchIds(String query, Pageable pageable) {
        if (searchHealthIndicator.isOpenSearchAvailable()) {
            log.debug("search_adapter=OpenSearch");
            return openSearchAdapter.searchIds(query, pageable);
        }
        log.debug("search_adapter=Postgres reason=opensearch_unavailable");
        return postgresSearchAdapter.searchIds(query, pageable);
    }
}