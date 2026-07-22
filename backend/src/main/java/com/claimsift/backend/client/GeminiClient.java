package com.claimsift.backend.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.claimsift.backend.config.properties.GeminiProperties;
import com.claimsift.backend.dto.gemini.GeminiGenerateContentRequest;
import com.claimsift.backend.dto.gemini.GeminiGenerateContentResponse;

@Component
public class GeminiClient {
    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiClient(RestClient.Builder restClientBuilder, GeminiProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public GeminiGenerateContentResponse generateContent(GeminiGenerateContentRequest request) {
        return restClient
                .post()
                .uri(properties.getModelContentPath(), properties.getModel())
                .header(properties.getApiKeyHeader(), properties.getApiKey())
                .body(request)
                .retrieve()
                .body(GeminiGenerateContentResponse.class);
    }
}