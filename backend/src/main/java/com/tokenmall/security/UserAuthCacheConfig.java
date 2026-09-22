package com.tokenmall.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class UserAuthCacheConfig {

    @Bean
    public Cache<Long, UserAuthSnapshot> userAuthLocalCache(UserAuthCacheProperties properties) {
        return Caffeine.newBuilder()
                .maximumSize(properties.getLocalMaximumSize())
                .expireAfterWrite(Duration.ofSeconds(properties.getLocalTtlSeconds()))
                .recordStats()
                .build();
    }
}
