package com.atamanahmet.vinylexchange.service.media;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.atamanahmet.vinylexchange.dto.musicbrainz.CoverArtResponse;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ImageMeta;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@EnableAsync
public class CoverArtService {

    private final Logger logger = LoggerFactory.getLogger(CoverArtService.class);
    private final WebClient webClient;
    private final FileStorageService fileStorageService;

    @Async
    public void fetchAndSaveCoverAsync(UUID listingId, UUID mbId) {

        if(mbId==null){
            logger.info("MBId is null, coverArt fetch aborted");

            return;
        }

        try {

            String imageUrl = fetchCoverUrl(mbId);

            if (imageUrl == null)
                return;

            ImageSource image = fileStorageService.downloadExternalImage(imageUrl);
            if (image == null)
                return;

            fileStorageService.savePlaceholderImage(image, mbId);

            logger.info("Cover art saved for listingId: {}, mbId: {}", listingId, mbId);

        } catch (Exception e) {
            logger.warn("Cover art fetch failed for listingId: {}, mbId: {}", listingId, mbId, e);
        }
    }

    private String fetchCoverUrl(UUID mbId) {

        CoverArtResponse response = webClient.get()
                .uri("https://coverartarchive.org/release/{id}?fmt=json", mbId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CoverArtResponse.class)
                .block();

        if (response == null || response.images() == null)
            return null;

        return response.images().stream()
                .filter(ImageMeta::front)
                .map(ImageMeta::image)
                .findFirst()
                .orElse(null);
    }
}
