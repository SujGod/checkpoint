package com.claimsift.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "claimsift.fact-check")
public class ClaimSiftFactCheckProperties {
    private int maxClaimsPerVideo;
    private int maxGoogleQueriesPerVideo;
    private double minimumMatchSimilarity;
}