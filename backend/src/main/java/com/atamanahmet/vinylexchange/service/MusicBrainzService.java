package com.atamanahmet.vinylexchange.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.atamanahmet.vinylexchange.external.musicbrainz.MusicBrainzClient;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.musicbrainz.Release;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ReleaseDTO;
import com.atamanahmet.vinylexchange.dto.musicbrainz.RootResponse;
import com.atamanahmet.vinylexchange.mapper.MediaInfoMapper;
import com.atamanahmet.vinylexchange.mapper.MusicBrainzFormatParser;

@Service
public class MusicBrainzService {

    private final MusicBrainzClient musicBrainzClient;

    public MusicBrainzService(MusicBrainzClient musicBrainzClient) {
        this.musicBrainzClient = musicBrainzClient;
    }

    public List<ReleaseDTO> searchReleases(String query, String scope, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        if (!supportsOffsetPagination(limit, offset)) {
            return List.of();
        }

        String luceneQuery = buildLuceneQuery(query.trim(), scope);
        RootResponse rootResponse = musicBrainzClient.searchReleases(luceneQuery, limit, offset);

        if (rootResponse == null || rootResponse.getReleases() == null) {
            return List.of();
        }

        List<Release> updatedReleases = rootResponse.getReleases().stream()
                .sorted(Comparator.comparingInt(Release::getScore).reversed())
                .peek(release -> release
                        .setExternalCoverUrl("http://coverartarchive.org/release/" + release.getId() + "/front-250"))
                .collect(Collectors.toList());

        return convertToDTO(updatedReleases);
    }

    /**
     * MusicBrainz uses limit/offset paging; reject invalid values before calling the API.
     */
    public static boolean supportsOffsetPagination(int limit, int offset) {
        return limit > 0 && offset >= 0;
    }

    private String buildLuceneQuery(String cleanQuery, String scope) {
        String normalizedScope = normalizeScope(scope);
        String escaped = escapeLucene(cleanQuery);
        String quoted = "\"" + escaped + "\"";

        String fieldQuery = switch (normalizedScope) {
            case "artist" -> "artist:" + quoted;
            case "both" -> "(release:" + quoted + " OR artist:" + quoted + ")";
            default -> "release:" + quoted;
        };

        return fieldQuery + " AND primarytype:album AND NOT title:Tribute";
    }

    private String normalizeScope(String scope) {
        if (scope == null) {
            return "title";
        }

        return switch (scope.trim().toLowerCase()) {
            case "artist", "both" -> scope.trim().toLowerCase();
            default -> "title";
        };
    }

    private String escapeLucene(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private List<ReleaseDTO> convertToDTO(List<Release> releases) {

        List<ReleaseDTO> releaseDTOs = releases.stream()
                .map(release -> {
                    String rawFormat = release.getMedia() != null && !release.getMedia().isEmpty()
                            ? release.getMedia().get(0).getFormat()
                            : null;
                    return ReleaseDTO.builder()
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
                        .suggestedMediaInfo(MediaInfoMapper.toDtoStatic(
                                MusicBrainzFormatParser.fromMusicBrainzFormat(rawFormat)))
                        .build();
                })
                .toList();

        return releaseDTOs;
    }

    private Integer extractYear(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }

        return Integer.parseInt(date.substring(0, 4));
    }
}
