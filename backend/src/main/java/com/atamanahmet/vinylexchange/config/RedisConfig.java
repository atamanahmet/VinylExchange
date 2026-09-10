package com.atamanahmet.vinylexchange.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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
     * Safety net expiry only, mutations use CacheEvict to rebuild right away
     */
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    /**
     * Write path manager, transaction aware, only the 9 CacheEvict on ListingService use this
     */
    @Primary
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> jsonSerializer = redisJsonSerializer();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig(jsonSerializer))
                .transactionAware()
                .build();
    }

    /**
     * Read only manager, not transaction aware, put runs inside CacheInterceptor so SoftFailCacheErrorHandler can catch a Redis failure
     */
    @Bean
    public RedisCacheManager readCacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> jsonSerializer = redisJsonSerializer();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig(jsonSerializer))
                .withCacheConfiguration("listings", cacheConfig(jsonSerializer))
                .withCacheConfiguration("countryOptions", cacheConfig(jsonSerializer))
                .withCacheConfiguration("genreOptions", cacheConfig(jsonSerializer))
                .withCacheConfiguration("coverArtUrls", cacheConfig(jsonSerializer))
                .build();
    }

    private static RedisCacheConfiguration cacheConfig(RedisSerializer<Object> valueSerializer) {
        return baseCacheConfig(valueSerializer).entryTtl(CACHE_TTL);
    }

    /**
     * Dedicated Redis ObjectMapper, never reuse the HTTP ObjectMapper, typing would leak to APIs
     * DefaultTyping EVERYTHING needed because cached DTOs are Java records
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