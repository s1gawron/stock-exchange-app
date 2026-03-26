package com.s1gawron.stockexchange.configuration;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfiguration {

    public static final String STOCK_SEARCH_CACHE = "stockSearchCache";

    public static final String STOCK_DATA_CACHE = "stockDataCache";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
            .withCacheConfiguration(STOCK_SEARCH_CACHE, getRedisCacheConfiguration(Duration.ofDays(1)))
            .withCacheConfiguration(STOCK_DATA_CACHE, getRedisCacheConfiguration(Duration.ofSeconds(15)));
    }

    private org.springframework.data.redis.cache.RedisCacheConfiguration getRedisCacheConfiguration(final Duration duration) {
        return org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(duration)
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

}
