package com.atamanahmet.vinylexchange.infrastructure.search.service;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchHealthIndicator {

    private final Logger log = LoggerFactory.getLogger(SearchHealthIndicator.class);
    private final RestHighLevelClient openSearchClient;

    @Getter
    private volatile boolean openSearchAvailable = false;

    /**
     * Check every 30 seconds
     */
    @Scheduled(fixedDelay = 30_000)
    public void check() {
        try {
            boolean alive = openSearchClient.ping(RequestOptions.DEFAULT);
            if (alive != openSearchAvailable) {
                log.info("OpenSearch status changed: {}", alive ? "UP" : "DOWN");
            }
            openSearchAvailable = alive;
        } catch (Exception e) {
            if (openSearchAvailable) {
                log.warn("OpenSearch went down: {}", e.getMessage());
            }
            openSearchAvailable = false;
        }
    }
}