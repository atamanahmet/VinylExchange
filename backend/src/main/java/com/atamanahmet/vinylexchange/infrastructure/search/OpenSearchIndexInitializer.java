package com.atamanahmet.vinylexchange.infrastructure.search;

import com.atamanahmet.vinylexchange.infrastructure.search.service.SearchHealthIndicator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Creates the listings index on startup if it does not exist.
 * Skips silently if OpenSearch is not available (cloud/demo env).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenSearchIndexInitializer {

    private static final String INDEX_NAME = "listings";

    private final RestHighLevelClient openSearchClient;
    private final SearchHealthIndicator searchHealthIndicator;

    @PostConstruct
    public void init() {
        if (!searchHealthIndicator.isOpenSearchAvailable()) {
            log.info("opensearch_unavailable index_create_skipped");
            return;
        }
        createIndexIfNotExists();
    }

    private void createIndexIfNotExists() {
        try {
            boolean exists = openSearchClient.indices()
                    .exists(new GetIndexRequest(INDEX_NAME), RequestOptions.DEFAULT);

            if (exists) {
                log.info("search_index_exists index={}", INDEX_NAME);
                return;
            }

            log.info("search_index_creating index={}", INDEX_NAME);

            CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);

            request.settings(Settings.builder()
                    .put("analysis.analyzer.autocomplete.tokenizer", "autocomplete_tokenizer")
                    .putList("analysis.analyzer.autocomplete.filter", "lowercase")
                    .put("analysis.tokenizer.autocomplete_tokenizer.type", "edge_ngram")
                    .put("analysis.tokenizer.autocomplete_tokenizer.min_gram", 3)
                    .put("analysis.tokenizer.autocomplete_tokenizer.max_gram", 20)
                    .putList("analysis.tokenizer.autocomplete_tokenizer.token_chars", "letter", "digit"));

            String mappingJson = """
                    {
                      "properties": {
                        "title":      {"type":"text", "analyzer":"autocomplete", "search_analyzer":"standard"},
                        "artistName": {"type":"text", "analyzer":"autocomplete", "search_analyzer":"standard"},
                        "labelName":  {"type":"text"},
                        "status":     {"type":"keyword"},
                        "createdAt":  {"type":"date"},
                        "price":      {"type":"long"}
                      }
                    }
                    """;

            request.mapping(mappingJson, XContentType.JSON);

            CreateIndexResponse response = openSearchClient.indices()
                    .create(request, RequestOptions.DEFAULT);

            log.info("search_index_created index={} acknowledged={}", INDEX_NAME, response.isAcknowledged());

        } catch (Exception e) {
            log.error("search_index_creation_failed index={} reason={}", INDEX_NAME, e.getMessage(), e);
        }
    }
}