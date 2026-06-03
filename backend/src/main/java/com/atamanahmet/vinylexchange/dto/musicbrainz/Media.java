package com.atamanahmet.vinylexchange.dto.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Media {

    private String id;

    private String format;

    @JsonProperty("disk-count")
    private Integer discCount;

    @JsonProperty("track-count")
    private Integer trackCount;

}
