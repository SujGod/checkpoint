package com.claimsift.backend.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.claimsift.backend.config.properties.GeminiProperties;
import com.claimsift.backend.dto.gemini.GeminiGenerateContentRequest;
import com.claimsift.backend.dto.gemini.GeminiGenerateContentResponse;

@Component
public class GeminiClient {

    private static final String API_KEY_HEADER = "x-goog-api-key";
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
                .uri(
                        "/models/{model}:generateContent",
                        properties.getModel()
                )
                .header(
                        API_KEY_HEADER,
                        properties.getApiKey()
                )
                .body(request)
                .retrieve()
                .body(
                        GeminiGenerateContentResponse.class
                );
    }
}