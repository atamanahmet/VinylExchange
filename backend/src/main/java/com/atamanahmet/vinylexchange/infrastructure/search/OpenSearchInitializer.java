package com.atamanahmet.vinylexchange.infrastructure.search;

import java.net.HttpURLConnection;
import java.net.URL;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

// TODO: remove, devonly
@Component
@RequiredArgsConstructor
public class OpenSearchInitializer {

    private final Logger log = LoggerFactory.getLogger(OpenSearchInitializer.class);

    private final RestHighLevelClient openSearchClient;

    private int retries = 2;

    @PostConstruct
    public void init() {
        try {
            if (!isOpenSearchRunning()) {
                waitForOpenSearch();
            }
            log.info("OpenSearch client container is running");
        } catch (Exception e) {
            log.warn("OpenSearch not available, starting without search: {}", e.getMessage());
        }
    }

    private void startDockerCompose() {

        String projectRoot = System.getProperty("user.dir");

        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "compose",
                    "-f",
                    projectRoot + "/infrastructure/search/docker/docker-compose.opensearch.yml",
                    "up", "-d");

            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to start OpenSearch via Docker", e);
        }
    }

    private void waitForOpenSearch() {
        while (retries-- > 0) {
            try {
                if (openSearchClient.ping(RequestOptions.DEFAULT)) {
                    return;
                }
            } catch (Exception e) {
                log.warn("OpenSearch not ready, retrying... ({} attempts left), reason: {}", retries, e.getMessage());
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new IllegalStateException("OpenSearch is not ready");
    }

    private boolean isOpenSearchRunning() {
        try {
            openSearchClient.ping(RequestOptions.DEFAULT);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
