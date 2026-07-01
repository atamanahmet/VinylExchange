package com.atamanahmet.vinylexchange.service;

import com.atamanahmet.vinylexchange.dto.musicbrainz.ArtistCredit;
import com.atamanahmet.vinylexchange.dto.musicbrainz.Label;
import com.atamanahmet.vinylexchange.dto.musicbrainz.LabelInfo;
import com.atamanahmet.vinylexchange.dto.musicbrainz.Media;
import com.atamanahmet.vinylexchange.dto.musicbrainz.Release;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ReleaseDTO;
import com.atamanahmet.vinylexchange.dto.musicbrainz.RootResponse;
import com.atamanahmet.vinylexchange.dto.musicbrainz.Tags;
import com.atamanahmet.vinylexchange.external.musicbrainz.MusicBrainzClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicBrainzServiceTest {

    @Mock
    private MusicBrainzClient musicBrainzClient;

    @InjectMocks
    private MusicBrainzService musicBrainzService;

    private UUID firstReleaseId;
    private UUID secondReleaseId;

    @BeforeEach
    void setUp() {
        firstReleaseId = UUID.fromString("a1000001-0001-4001-8001-000000000001");
        secondReleaseId = UUID.fromString("a1000002-0002-4002-8002-000000000002");
    }

    @Test
    void searchReleases_blankQuery_returnsEmptyWithoutCallingClient() {
        List<ReleaseDTO> result = musicBrainzService.searchReleases("   ", "title", 75, 0);

        assertThat(result).isEmpty();
        verifyNoInteractions(musicBrainzClient);
    }

    @Test
    void searchReleases_nullQuery_returnsEmptyWithoutCallingClient() {
        List<ReleaseDTO> result = musicBrainzService.searchReleases(null, "title", 75, 0);

        assertThat(result).isEmpty();
        verifyNoInteractions(musicBrainzClient);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "-1, 0",
            "75, -1"
    })
    void searchReleases_invalidPagination_returnsEmptyWithoutCallingClient(int limit, int offset) {
        assertThat(MusicBrainzService.supportsOffsetPagination(limit, offset)).isFalse();

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", limit, offset);

        assertThat(result).isEmpty();
        verifyNoInteractions(musicBrainzClient);
    }

    @Test
    void supportsOffsetPagination_acceptsFirstPageAndFollowUpPages() {
        assertThat(MusicBrainzService.supportsOffsetPagination(75, 0)).isTrue();
        assertThat(MusicBrainzService.supportsOffsetPagination(75, 75)).isTrue();
        assertThat(MusicBrainzService.supportsOffsetPagination(75, 150)).isTrue();
    }

    @Test
    void searchReleases_mapsReleasesToDtoWithCoverUrlAndYear() {
        Release release = release(firstReleaseId, 100, "Abbey Road", "1969-09-26");
        stubClientResponse(List.of(release), 75, 0);

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(firstReleaseId);
        assertThat(result.get(0).getTitle()).isEqualTo("Abbey Road");
        assertThat(result.get(0).getYear()).isEqualTo(1969);
        assertThat(result.get(0).getExternalCoverUrl())
                .isEqualTo("http://coverartarchive.org/release/" + firstReleaseId + "/front-250");
    }

    @Test
    void searchReleases_sortsByScoreDescending() {
        Release lowerScore = release(firstReleaseId, 50, "Lower score release", "1970");
        Release higherScore = release(secondReleaseId, 99, "Higher score release", "1971");
        stubClientResponse(List.of(lowerScore, higherScore), 75, 0);

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertThat(result).extracting(ReleaseDTO::getId)
                .containsExactly(secondReleaseId, firstReleaseId);
    }

    @Test
    void searchReleases_nullApiResponse_returnsEmptyList() {
        when(musicBrainzClient.searchReleases(anyString(), anyInt(), anyInt())).thenReturn(null);

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void searchReleases_nullReleaseList_returnsEmptyList() {
        when(musicBrainzClient.searchReleases(anyString(), anyInt(), anyInt()))
                .thenReturn(new RootResponse());

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void searchReleases_titleScope_buildsReleaseFieldQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Abbey Road", "1969")), 75, 0);

        musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertClientCalledWithQuery(
                "release:\"abbey road\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_artistScope_buildsArtistFieldQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Abbey Road", "1969")), 75, 0);

        musicBrainzService.searchReleases("beatles", "artist", 75, 0);

        assertClientCalledWithQuery(
                "artist:\"beatles\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_bothScope_buildsCombinedFieldQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Abbey Road", "1969")), 75, 0);

        musicBrainzService.searchReleases("abbey road", "both", 75, 0);

        assertClientCalledWithQuery(
                "(release:\"abbey road\" OR artist:\"abbey road\") AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_escapesQuotesInQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Test", "1970")), 75, 0);

        musicBrainzService.searchReleases("say \"hello\"", "title", 75, 0);

        assertClientCalledWithQuery(
                "release:\"say \\\"hello\\\"\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_offsetPagination_passesOffsetToClient() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Page one", "1969")), 75, 0);
        stubClientResponse(List.of(release(secondReleaseId, 80, "Page two", "1970")), 75, 75);

        List<ReleaseDTO> firstPage = musicBrainzService.searchReleases("abbey road", "title", 75, 0);
        List<ReleaseDTO> secondPage = musicBrainzService.searchReleases("abbey road", "title", 75, 75);

        assertThat(firstPage).extracting(ReleaseDTO::getId).containsExactly(firstReleaseId);
        assertThat(secondPage).extracting(ReleaseDTO::getId).containsExactly(secondReleaseId);

        ArgumentCaptor<Integer> offsetCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(musicBrainzClient, times(2))
                .searchReleases(anyString(), eq(75), offsetCaptor.capture());
        assertThat(offsetPaginationWorks(offsetCaptor.getAllValues()))
                .as("offset paging should forward 0 then 75 to MusicBrainz")
                .isTrue();
    }

    @Test
    void searchReleases_trimsQueryBeforeSearch() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Abbey Road", "1969")), 75, 0);

        musicBrainzService.searchReleases("  abbey road  ", "title", 75, 0);

        assertClientCalledWithQuery(
                "release:\"abbey road\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_nullScope_defaultsToTitleQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Abbey Road", "1969")), 75, 0);

        musicBrainzService.searchReleases("abbey road", null, 75, 0);

        assertClientCalledWithQuery(
                "release:\"abbey road\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_unknownScope_defaultsToTitleQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Abbey Road", "1969")), 75, 0);

        musicBrainzService.searchReleases("abbey road", "label", 75, 0);

        assertClientCalledWithQuery(
                "release:\"abbey road\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_escapesBackslashInQuery() {
        stubClientResponse(List.of(release(firstReleaseId, 90, "Test", "1970")), 75, 0);

        musicBrainzService.searchReleases("path\\to", "title", 75, 0);

        assertClientCalledWithQuery(
                "release:\"path\\\\to\" AND primarytype:album AND NOT title:Tribute",
                75,
                0);
    }

    @Test
    void searchReleases_mapsNullDateToNullYear() {
        Release release = release(firstReleaseId, 90, "Unknown year", null);
        stubClientResponse(List.of(release), 75, 0);

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertThat(result.get(0).getYear()).isNull();
    }

    @Test
    void searchReleases_mapsEmptyDateToNullYear() {
        Release release = release(firstReleaseId, 90, "Unknown year", "");
        stubClientResponse(List.of(release), 75, 0);

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        assertThat(result.get(0).getYear()).isNull();
    }

    @Test
    void searchReleases_mapsAllDtoFields() {
        Release release = release(firstReleaseId, 90, "Abbey Road", "1969-09-26");
        release.setBarcode("123");
        release.setTrackCount(17);
        LabelInfo labelInfo = new LabelInfo();
        labelInfo.setLabel(new Label("label-id", "Apple Corps"));
        release.setLabelInfo(List.of(labelInfo));
        Media media = new Media();
        media.setFormat("12\" Vinyl");
        release.setMedia(List.of(media));
        release.setTags(List.of(new Tags("rock")));
        stubClientResponse(List.of(release), 75, 0);

        List<ReleaseDTO> result = musicBrainzService.searchReleases("abbey road", "title", 75, 0);

        ReleaseDTO dto = result.get(0);
        assertThat(dto.getBarcode()).isEqualTo("123");
        assertThat(dto.getCountry()).isEqualTo("US");
        assertThat(dto.getTrackCount()).isEqualTo(17);
        assertThat(dto.getLabelInfo()).hasSize(1);
        assertThat(dto.getMedia().get(0).getFormat()).isEqualTo("12\" Vinyl");
        assertThat(dto.getSuggestedMediaInfo()).isNotNull();
        assertThat(dto.getSuggestedMediaInfo().getFormat().name()).isEqualTo("VINYL");
        assertThat(dto.getTags()).hasSize(1);
    }

    private Release release(UUID id, int score, String title, String date) {
        Release release = new Release();
        release.setId(id);
        release.setScore(score);
        release.setTitle(title);
        release.setDate(date);
        release.setCountry("US");
        release.setArtistCredit(List.of(new ArtistCredit("The Beatles")));
        return release;
    }

    private void stubClientResponse(List<Release> releases, int limit, int offset) {
        RootResponse response = new RootResponse();
        response.setReleases(releases);
        when(musicBrainzClient.searchReleases(anyString(), eq(limit), eq(offset)))
                .thenReturn(response);
    }

    private void assertClientCalledWithQuery(String expectedQuery, int limit, int offset) {
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(musicBrainzClient).searchReleases(queryCaptor.capture(), eq(limit), eq(offset));
        assertThat(queryCaptor.getValue()).isEqualTo(expectedQuery);
    }

    /**
     * Checks that MusicBrainz offset paging forwarded the expected page offsets.
     */
    static boolean offsetPaginationWorks(List<Integer> capturedOffsets) {
        return capturedOffsets.size() >= 2
                && capturedOffsets.get(0) == 0
                && capturedOffsets.get(1) == 75
                && MusicBrainzService.supportsOffsetPagination(75, capturedOffsets.get(1));
    }
}
