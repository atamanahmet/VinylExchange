package com.atamanahmet.vinylexchange.external.musicbrainz;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.atamanahmet.vinylexchange.dto.musicbrainz.RootResponse;

import reactor.util.retry.Retry;

@Component
public class MusicBrainzClient {
        public RootResponse searchTitle(String title, int limit) {

                WebClient client = WebClient.builder()
                                .baseUrl("https://musicbrainz.org/ws/2/release")
                                .defaultHeader("User-Agent", "testApp/0.1 (foxitrot42@gmail.com)")
                                .build();

                RootResponse result = client.get()
                                .uri(uriBuilder -> uriBuilder
                                                .queryParam("query", "release:\"" + title
                                                                + "\" AND primarytype:album AND NOT title:Tribute")
                                                .queryParam("fmt", "json")
                                                // .queryParam("inc", "ratings")
                                                .queryParam("limit", 75)
                                                .build())
                                .retrieve()
                                .bodyToMono(RootResponse.class)
                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                                .maxBackoff(Duration.ofSeconds(10))
                                                .filter(throwable -> throwable instanceof WebClientResponseException
                                                                || throwable.getMessage().contains(
                                                                                "Connection reset")))
                                .timeout(Duration.ofSeconds(30)) // Add timeout
                                .block();

                return result;
        }
}
