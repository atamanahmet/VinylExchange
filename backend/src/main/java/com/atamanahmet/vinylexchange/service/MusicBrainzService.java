package com.atamanahmet.vinylexchange.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.external.musicbrainz.MusicBrainzClient;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.musicbrainz.Release;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ReleaseDTO;
import com.atamanahmet.vinylexchange.dto.musicbrainz.RootResponse;

@Service
public class MusicBrainzService {

    private final MusicBrainzClient musicBrainzClient;

    public MusicBrainzService(MusicBrainzClient musicBrainzClient) {
        this.musicBrainzClient = musicBrainzClient;
    }

    public List<ReleaseDTO> searchTitle(String title) {

        String cleanTitle = title.trim();

        String encoded = URLEncoder.encode(cleanTitle, StandardCharsets.UTF_8);

        RootResponse RootResponse = musicBrainzClient.searchTitle(encoded, 15);

        List<Release> releases = RootResponse.getReleases();

        List<Release> updatedReleases = releases.stream()
                .sorted(Comparator.comparingInt(Release::getScore).reversed())
                // .limit(5)
                .peek(release -> release
                        .setExternalCoverUrl("http://coverartarchive.org/release/" + release.getId() + "/front-250"))
                .collect(Collectors.toList());

        return convertToDTO(updatedReleases);
    }

    private List<ReleaseDTO> convertToDTO(List<Release> releases) {

        List<ReleaseDTO> releaseDTOs = releases.stream()
                .map(release -> ReleaseDTO.builder()
                        .id(release.getId())
                        .title(release.getTitle())
                        .artistCredit(release.getArtistCredit())
                        .externalCoverUrl(release.getExternalCoverUrl())
                        .year(extractYear(release.getDate()))
                        .country(release.getCountry())
                        .barcode(release.getBarcode())
                        .labelInfo(release.getLabelInfo())
                        .trackCount(release.getTrackCount())
                        .media(release.getMedia())
                        .tags(release.getTags())
                        .build())
                .toList();

        return releaseDTOs;
    }

    // exract only year, mb api is not consistent abvout date
    private Integer extractYear(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }

        return Integer.parseInt(date.substring(0, 4));
    }
}
