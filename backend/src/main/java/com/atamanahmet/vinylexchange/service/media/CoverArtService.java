package com.atamanahmet.vinylexchange.service.media;

import com.atamanahmet.vinylexchange.dto.musicbrainz.CoverArtResponse;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ImageMeta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverArtService {

    private final WebClient webClient;

    /**
     * Fetches front cover URL from coverartarchive.org
     * Returns null if not found, caller decides what to do
     */
    public String fetchCoverUrl(UUID mbId) {
        if (mbId == null) return null;

        try {
            CoverArtResponse response = webClient.get()
                    .uri("https://coverartarchive.org/release/{id}?fmt=json", mbId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(CoverArtResponse.class)
                    .block();

            if (response == null || response.images() == null) return null;

            return response.images().stream()
                    .filter(ImageMeta::front)
                    .map(ImageMeta::image)
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            log.warn("CoverArt fetch failed for mbId: {}", mbId);
            return null;
        }
    }
}