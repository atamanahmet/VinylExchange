package com.atamanahmet.vinylexchange.infrastructure.search.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

public interface SearchPort {

    /**
     * Returns ordered listing IDs matching query
     */
    Page<UUID> searchIds(String query, int page, int size);
}