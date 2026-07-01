package com.atamanahmet.vinylexchange.external.musicbrainz;

import org.springframework.stereotype.Component;

import com.atamanahmet.vinylexchange.config.MusicBrainzProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MusicBrainzRateLimiter {

    private final MusicBrainzProperties musicBrainzProperties;

    private final Object lock = new Object();

    private long lastRequestAtMs = 0;

    public void awaitPermit() {
        MusicBrainzProperties.RateLimit rateLimit = musicBrainzProperties.getRateLimit();
        if (rateLimit == null || !rateLimit.isEnabled()) {
            return;
        }

        long minIntervalMs = Math.max(0, rateLimit.getMinIntervalMs());

        synchronized (lock) {
            long elapsed = System.currentTimeMillis() - lastRequestAtMs;
            long waitMs = minIntervalMs - elapsed;
            if (waitMs > 0) {
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("MusicBrainz rate-limit wait interrupted", interrupted);
                }
            }
            lastRequestAtMs = System.currentTimeMillis();
        }
    }
}
