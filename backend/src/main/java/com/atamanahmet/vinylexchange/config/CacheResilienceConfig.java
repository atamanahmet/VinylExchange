package com.atamanahmet.vinylexchange.config;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Softens cache failures so a Redis outage does not fail the request.
 */
@Slf4j
@Configuration
public class CacheResilienceConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SoftFailCacheErrorHandler();
    }

    private static final class SoftFailCacheErrorHandler extends SimpleCacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache get failed for cache={} key={}", cache.getName(), key, exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Cache put failed for cache={} key={}", cache.getName(), key, exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache evict failed for cache={} key={}", cache.getName(), key, exception);
        }
    }
}
