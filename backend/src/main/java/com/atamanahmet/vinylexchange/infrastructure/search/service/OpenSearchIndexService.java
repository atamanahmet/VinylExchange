package com.atamanahmet.vinylexchange.infrastructure.search.service;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.common.xcontent.XContentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.atamanahmet.vinylexchange.infrastructure.search.document.ListingDocument;
import com.atamanahmet.vinylexchange.domain.entity.Listing;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchIndexService {


    private final RestHighLevelClient openSearchClient;
    private final ObjectMapper objectMapper;
    private final SearchHealthIndicator searchHealthIndicator;

    private static final String INDEX_NAME = "listings";

    @Async
    public void indexListing(Listing listing) {

        if (!searchHealthIndicator.isOpenSearchAvailable()) {
            log.debug("index_skipped listing_id={} reason=opensearch_unavailable", listing.getId());
            return;
        }

        try {
            ListingDocument listingDocument = convertToDocument(listing);
            String documentJson = objectMapper.writeValueAsString(listingDocument);

            IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                    .id(listing.getId().toString())
                    .source(documentJson, XContentType.JSON);

            IndexResponse indexResponse = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);

            log.info("index_success listing_id={} result={}", indexResponse.getId(), indexResponse.getResult());

        } catch (IOException e) {
            log.error("index_failed listing_id={} reason={}", listing.getId(), e.getMessage(), e);
        }
    }

    private ListingDocument convertToDocument(Listing listing) {
        return ListingDocument.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .artistName(listing.getArtistName())
                .year(listing.getYear())
                .status(listing.getStatus().name())
                .country(listing.getCountry())
                .format(listing.getFormat())
                .labelName(listing.getLabelName())
                .barcode(listing.getBarcode())
                .ownerUsername(listing.getOwnerUsername())
                .isPromoted(listing.isPromote())
                .price(listing.getPriceKurus())
                .createdAt(listing.getCreatedAt())
                .build();
    }
}