package com.atamanahmet.vinylexchange.infrastructure.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final Optional<OpenSearchAdapter> openSearchAdapter;
    private final Optional<SearchHealthIndicator> searchHealthIndicator;

    @Value("${opensearch.enabled:false}")
    private boolean openSearchEnabled;

    @Override
    public Page<UUID> searchIds(String query, Pageable pageable) {
        boolean useOpenSearch = openSearchEnabled
                && searchHealthIndicator.map(SearchHealthIndicator::isOpenSearchAvailable).orElse(false);
        if (useOpenSearch) {
            log.debug("search_adapter=OpenSearch");
            return openSearchAdapter.get().searchIds(query, pageable);
        }
        log.debug("search_adapter=Postgres reason={}",
                openSearchEnabled ? "opensearch_unavailable" : "opensearch_disabled");
        return postgresSearchAdapter.searchIds(query, pageable);
    }
}
