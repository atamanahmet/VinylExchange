package com.atamanahmet.vinylexchange.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Safety-net expiry only. Mutations use {@code @CacheEvict} to rebuild immediately;
     * TTL exists so a missed eviction cannot leave data forever.
     */
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        RedisSerializer<Object> jsonSerializer = redisJsonSerializer();
        RedisCacheConfiguration defaults = baseCacheConfig(jsonSerializer).entryTtl(CACHE_TTL);

        return builder -> builder
                .cacheDefaults(defaults)
                .transactionAware()
                .withCacheConfiguration("listings",
                        baseCacheConfig(jsonSerializer).entryTtl(CACHE_TTL))
                .withCacheConfiguration("countryOptions",
                        baseCacheConfig(jsonSerializer).entryTtl(CACHE_TTL))
                .withCacheConfiguration("genreOptions",
                        baseCacheConfig(jsonSerializer).entryTtl(CACHE_TTL));
    }

    /**
     * Dedicated Redis ObjectMapper — never reuse the HTTP ObjectMapper (typing would leak to APIs).
     * DefaultTyping.EVERYTHING required because cached DTOs are Java records (final).
     */
    private static RedisSerializer<Object> redisJsonSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.atamanahmet.vinylexchange.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.math.")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);

        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    private static RedisCacheConfiguration baseCacheConfig(RedisSerializer<Object> valueSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }
}
