package com.claimsift.backend.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.claimsift.backend.model.CachedFactCheckResult;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class FactCheckCacheConfig {

    @Bean
    public Cache<String, CachedFactCheckResult> factCheckResultCache() {

        return Caffeine.newBuilder()
                .maximumSize(25_000)
                .expireAfterWrite(Duration.ofHours(24))
                .recordStats()
                .build();
    }
}