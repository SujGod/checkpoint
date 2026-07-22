package com.claimsift.backend.config.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private int maxClaimsPerChunk;
    private int maxOutputTokens;

    @PostConstruct
    public void validate() {
        if (StringUtils.isEmpty(baseUrl)) {
            throw new IllegalStateException("Gemini base URL is not configured.");
        }

        if (StringUtils.isEmpty(apiKey)|| apiKey.contains("$") || apiKey.contains("GEMINI_API_KEY")) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }

        if (StringUtils.isEmpty(model)) {
            throw new IllegalStateException("Gemini model is not configured.");
        }

        if (maxClaimsPerChunk < 1) {
            throw new IllegalStateException("Gemini maxClaimsPerChunk must be at least 1.");
        }
    }
}