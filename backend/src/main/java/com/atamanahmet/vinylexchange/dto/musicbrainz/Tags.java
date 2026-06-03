package com.atamanahmet.vinylexchange.dto.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tags {

    @JsonProperty("name")
    private String name;
}
