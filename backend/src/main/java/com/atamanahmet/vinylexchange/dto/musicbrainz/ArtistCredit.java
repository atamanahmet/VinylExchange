package com.atamanahmet.vinylexchange.dto.musicbrainz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArtistCredit(String name) {}
