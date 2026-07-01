package com.atamanahmet.vinylexchange.dto.musicbrainz;

import com.atamanahmet.vinylexchange.dto.listing.MediaInfoDTO;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseDTO {

    private UUID id;

    private int score;

    private String title;

    private List<ArtistCredit> artistCredit;

    private String externalCoverUrl;

    private Integer year;

    private String country;

    private String barcode;

    private List<LabelInfo> labelInfo;

    private Integer trackCount;

    private List<Media> media;

    private MediaInfoDTO suggestedMediaInfo;

    private List<Tags> tags;

}
