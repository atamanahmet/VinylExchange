package com.atamanahmet.vinylexchange.dto.musicbrainz;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Release {

    @Id
    private UUID id;

    private int score;

    @JsonProperty("artist-credit-id")
    private String artistCreditId;

    private String title;

    @JsonAlias("artist-credit")
    private List<ArtistCredit> artistCredit;

    private String date;

    private String status;

    private String country;

    private String barcode;

    @JsonAlias("label-info")
    private List<LabelInfo> labelInfo;

    @JsonAlias("track-count")
    private Integer trackCount;

    private List<Media> media;

    private List<Tags> tags;

    private String externalCoverUrl;

}
