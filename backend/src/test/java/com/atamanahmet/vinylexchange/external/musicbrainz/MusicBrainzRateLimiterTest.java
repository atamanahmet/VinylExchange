package com.atamanahmet.vinylexchange.external.musicbrainz;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.atamanahmet.vinylexchange.config.MusicBrainzProperties;

class MusicBrainzRateLimiterTest {

    @Test
    void awaitPermit_whenDisabled_doesNotWait() {
        MusicBrainzProperties properties = new MusicBrainzProperties();
        properties.getRateLimit().setEnabled(false);
        MusicBrainzRateLimiter rateLimiter = new MusicBrainzRateLimiter(properties);

        long startedAt = System.currentTimeMillis();
        rateLimiter.awaitPermit();
        rateLimiter.awaitPermit();

        assertThat(System.currentTimeMillis() - startedAt).isLessThan(100);
    }

    @Test
    void awaitPermit_whenEnabled_waitsBetweenRequests() {
        MusicBrainzProperties properties = new MusicBrainzProperties();
        properties.getRateLimit().setEnabled(true);
        properties.getRateLimit().setMinIntervalMs(200);
        MusicBrainzRateLimiter rateLimiter = new MusicBrainzRateLimiter(properties);

        rateLimiter.awaitPermit();
        long startedAt = System.currentTimeMillis();
        rateLimiter.awaitPermit();

        assertThat(System.currentTimeMillis() - startedAt).isGreaterThanOrEqualTo(150);
    }
}
