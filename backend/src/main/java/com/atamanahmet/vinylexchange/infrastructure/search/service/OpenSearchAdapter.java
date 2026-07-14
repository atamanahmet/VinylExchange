package com.atamanahmet.vinylexchange.infrastructure.search.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.Fuzziness;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSearchAdapter implements SearchPort {

    private static final String INDEX_NAME = "listings";

    private final Logger log = LoggerFactory.getLogger(OpenSearchAdapter.class);
    private final RestHighLevelClient openSearchClient;

    @Override
    public Page<UUID> searchIds(String query, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        try {
            SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

            SearchSourceBuilder source = new SearchSourceBuilder()
                    .query(buildQuery(query))
                    .from(page * size)
                    .size(size);

            searchRequest.source(source);

            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);

            List<UUID> ids = Arrays.stream(response.getHits().getHits())
                    .map(hit -> UUID.fromString(hit.getId()))
                    .toList();

            long totalHits = response.getHits().getTotalHits().value;

            return new PageImpl<>(ids, pageable, totalHits);

        } catch (Exception e) {
            log.error("OpenSearch search failed", e);
            throw new RuntimeException("OpenSearch search failed", e);
        }
    }

    private QueryBuilder buildQuery(String query) {

        BoolQueryBuilder boolQuery = new BoolQueryBuilder();

        if (query == null || query.isBlank()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(
                    QueryBuilders.multiMatchQuery(query)
                            .field("title", 3.0f)
                            .field("artistName", 2.5f)
                            .field("labelName", 1.5f)
                            .field("format")
                            .operator(Operator.AND)
                            .fuzziness(Fuzziness.AUTO)
                            .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }

        boolQuery.filter(QueryBuilders.termQuery("status", "AVAILABLE"));

        return boolQuery;
    }
}