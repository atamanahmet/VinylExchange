package com.atamanahmet.vinylexchange.external.musicbrainz;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.atamanahmet.vinylexchange.config.MusicBrainzProperties;
import com.atamanahmet.vinylexchange.dto.musicbrainz.RootResponse;

import lombok.RequiredArgsConstructor;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class MusicBrainzClient {

        private final MusicBrainzProperties musicBrainzProperties;
        private final MusicBrainzRateLimiter musicBrainzRateLimiter;

        public RootResponse searchReleases(String luceneQuery, int limit, int offset) {
                musicBrainzRateLimiter.awaitPermit();

                WebClient client = WebClient.builder()
                                .baseUrl(musicBrainzProperties.getBaseUrl())
                                .defaultHeader("User-Agent", musicBrainzProperties.getUserAgent())
                                .build();

                RootResponse result = client.get()
                                .uri(uriBuilder -> uriBuilder
                                                .queryParam("query", luceneQuery)
                                                .queryParam("fmt", "json")
                                                .queryParam("limit", limit)
                                                .queryParam("offset", offset)
                                                .build())
                                .retrieve()
                                .bodyToMono(RootResponse.class)
                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                                .maxBackoff(Duration.ofSeconds(10))
                                                .filter(throwable -> throwable instanceof WebClientResponseException
                                                                || throwable.getMessage().contains(
                                                                                "Connection reset")))
                                .timeout(Duration.ofSeconds(30))
                                .block();

                return result;
        }
}
