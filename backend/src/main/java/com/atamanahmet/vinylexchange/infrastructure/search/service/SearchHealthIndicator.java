package com.atamanahmet.vinylexchange.infrastructure.search.service;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchHealthIndicator {

    private final RestHighLevelClient openSearchClient;

    @Getter
    private volatile boolean openSearchAvailable = false;

    /**
     * Initial check so router has correct state before first request
     */
    @PostConstruct
    public void init() {
        check();
    }

    /**
     * Check every 60 seconds
     */
    @Scheduled(fixedDelay = 60_000)
    public void check() {
        try {
            boolean alive = openSearchClient.ping(RequestOptions.DEFAULT);
            if (alive != openSearchAvailable) {
                log.info("opensearch_status_changed status={}", alive ? "UP" : "DOWN");
            }
            openSearchAvailable = alive;
        } catch (Exception e) {
            if (openSearchAvailable) {
                log.warn("opensearch_went_down reason={}", e.getMessage());
            }
            openSearchAvailable = false;
        }
    }
}