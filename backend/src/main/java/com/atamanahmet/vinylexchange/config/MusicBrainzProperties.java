package com.atamanahmet.vinylexchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "musicbrainz")
public class MusicBrainzProperties {

    private String baseUrl = "https://musicbrainz.org/ws/2/release";

    private String userAgent = "testApp/0.1 (foxitrot42@gmail.com)";

    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class RateLimit {
        private boolean enabled = true;
        private long minIntervalMs = 1100;
    }
}
