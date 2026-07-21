package com.claimsift.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "claimsift.fact-check")
public class ClaimSiftFactCheckProperties {
    private int maxGoogleQueriesPerVideo;
    private double minimumMatchSimilarity;
}