package com.claimsift.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "google.fact-check")
public class GoogleFactCheckProperties {
    private String baseUrl;
    private String apiKey;
    private String languageCode;
    private int pageSize;
}