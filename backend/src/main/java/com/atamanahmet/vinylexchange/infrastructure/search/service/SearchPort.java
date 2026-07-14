package com.atamanahmet.vinylexchange.infrastructure.search.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchPort {

    /**
     * Returns ordered listing IDs matching query
     */
    Page<UUID> searchIds(String query, Pageable pageable);
}
