package com.atamanahmet.vinylexchange.security.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atamanahmet.vinylexchange.exception.RateLimitExceededException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

/**
 * In-memory rate limits for PATCH /api/account/password only.
 * Separate buckets per user id and per client IP; both must allow the attempt.
 */
@Component
public class PasswordChangeRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final Duration window;

    public PasswordChangeRateLimiter(
            @Value("${app.rate-limit.password-change.capacity:5}") long capacity,
            @Value("${app.rate-limit.password-change.window-minutes:15}") long windowMinutes) {
        this.capacity = capacity;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public void checkAllowed(UUID userId, String clientIp) {
        Bucket userBucket = resolveBucket("user:" + userId);
        Bucket ipBucket = resolveBucket("ip:" + clientIp);

        if (!userBucket.tryConsume(1) || !ipBucket.tryConsume(1)) {
            throw new RateLimitExceededException();
        }
    }

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, ignored -> newBucket());
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, window)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
