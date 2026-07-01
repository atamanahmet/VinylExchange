package com.atamanahmet.vinylexchange.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.atamanahmet.vinylexchange.config.MusicBrainzProperties;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ReleaseDTO;
import com.atamanahmet.vinylexchange.external.musicbrainz.MusicBrainzClient;
import com.atamanahmet.vinylexchange.external.musicbrainz.MusicBrainzRateLimiter;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MusicBrainzIntegrationTest {

    private static final int PAGE_SIZE = 75;

    private MusicBrainzService musicBrainzService;

    @BeforeEach
    void setUp() {
        MusicBrainzProperties properties = new MusicBrainzProperties();
        MusicBrainzRateLimiter rateLimiter = new MusicBrainzRateLimiter(properties);
        MusicBrainzClient client = new MusicBrainzClient(properties, rateLimiter);
        musicBrainzService = new MusicBrainzService(client);
    }

    @Test
    @Order(1)
    void searchReleases_titleScope_returnsMappedLiveResults() {
        List<ReleaseDTO> results = musicBrainzService.searchReleases("abbey road", "title", 10, 0);

        assertThat(results).isNotEmpty();
        ReleaseDTO first = results.get(0);
        assertThat(first.getId()).isNotNull();
        assertThat(first.getTitle()).isNotBlank();
        assertThat(first.getArtistCredit()).isNotEmpty();
        assertThat(first.getExternalCoverUrl())
                .contains("coverartarchive.org/release/")
                .contains(first.getId().toString());
    }

    @Test
    @Order(2)
    void searchReleases_artistScope_returnsLiveResults() {
        List<ReleaseDTO> results = musicBrainzService.searchReleases("beatles", "artist", 10, 0);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getArtistCredit().get(0).name()).isNotBlank();
    }

    @Test
    @Order(3)
    void searchReleases_bothScope_returnsLiveResults() {
        List<ReleaseDTO> results = musicBrainzService.searchReleases("abbey road", "both", 10, 0);

        assertThat(results).isNotEmpty();
    }

    @Test
    @Order(4)
    void searchReleases_offsetPagination_returnsExpectedSecondPageFromMusicBrainz() {
        String query = "abbey road";

        List<ReleaseDTO> firstPage =
                musicBrainzService.searchReleases(query, "title", PAGE_SIZE, 0);
        List<ReleaseDTO> secondPage =
                musicBrainzService.searchReleases(query, "title", PAGE_SIZE, PAGE_SIZE);

        assertThat(firstPage)
                .as("first page should be a full MusicBrainz window")
                .hasSize(PAGE_SIZE);
        assertThat(secondPage)
                .as("second page should return another full window at offset %s", PAGE_SIZE)
                .hasSize(PAGE_SIZE);

        Set<UUID> firstPageIds =
                firstPage.stream().map(ReleaseDTO::getId).collect(Collectors.toSet());
        Set<UUID> secondPageIds =
                secondPage.stream().map(ReleaseDTO::getId).collect(Collectors.toSet());
        Set<UUID> combinedIds = new HashSet<>(firstPageIds);
        combinedIds.addAll(secondPageIds);

        assertThat(firstPageIds)
                .as("second page must not repeat first-page release IDs")
                .doesNotContainAnyElementsOf(secondPageIds);
        assertThat(combinedIds)
                .as("offset paging should append 75 new releases")
                .hasSize(PAGE_SIZE * 2);
        assertThat(secondPage.get(0).getId())
                .as("second page should start at a different release than the first page")
                .isNotEqualTo(firstPage.get(0).getId());
        assertThat(MusicBrainzService.supportsOffsetPagination(PAGE_SIZE, PAGE_SIZE)).isTrue();
    }

    @Test
    @Order(5)
    void searchReleases_liveResults_includeExpectedReleaseMetadata() {
        List<ReleaseDTO> results = musicBrainzService.searchReleases("nevermind", "title", 20, 0);

        assertThat(results).isNotEmpty();
        ReleaseDTO release = results.get(0);
        assertThat(release.getTitle()).isNotBlank();
        assertThat(release.getCountry()).isNotBlank();
    }
}
