package com.claimsift.backend.client;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.claimsift.backend.config.properties.GoogleFactCheckProperties;
import com.claimsift.backend.dto.google.GoogleClaimResponse;
import com.claimsift.backend.dto.google.GoogleFactCheckSearchResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleFactCheckClient {
    private final RestClient restClient;
    private final GoogleFactCheckProperties properties;

    public GoogleFactCheckClient(RestClient.Builder restClientBuilder, GoogleFactCheckProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public GoogleFactCheckSearchResponse search(String claimText) {
        if (StringUtils.isBlank(claimText)) {
            throw new IllegalArgumentException("Claim text must not be blank.");
        }

        GoogleFactCheckSearchResponse response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(properties.getClaimSearchPath())
                                .queryParam("query", claimText.trim())
                                .queryParam("languageCode", properties.getLanguageCode())
                                .queryParam("pageSize", properties.getPageSize())
                                .build()
                        )
                        .header(properties.getApiKeyHeader(), properties.getApiKey())
                        .retrieve()
                        .body(  GoogleFactCheckSearchResponse.class);

        if (Objects.isNull(response)) {
            log.info("[ClaimSift] Google returned an empty response body.");
            return new GoogleFactCheckSearchResponse();
        }

        return response;
    }
}