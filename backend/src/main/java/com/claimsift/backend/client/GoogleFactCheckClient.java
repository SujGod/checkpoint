package com.claimsift.backend.client;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.claimsift.backend.config.properties.GoogleFactCheckProperties;
import com.claimsift.backend.dto.GoogleClaimResponse;
import com.claimsift.backend.dto.GoogleFactCheckSearchResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleFactCheckClient {

    private static final String CLAIM_SEARCH_PATH = "/claims:search";
    private static final String API_KEY_HEADER = "x-goog-api-key";

    private final RestClient restClient;
    private final GoogleFactCheckProperties properties;

    public GoogleFactCheckClient(
            RestClient.Builder restClientBuilder,
            GoogleFactCheckProperties properties) {

        this.properties = properties;

        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public GoogleFactCheckSearchResponse search(
            String claimText) {

        if (StringUtils.isBlank(claimText)) {
            throw new IllegalArgumentException("Claim text must not be blank.");
        }

        GoogleFactCheckSearchResponse response =
                restClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(CLAIM_SEARCH_PATH)
                                .queryParam(
                                        "query",
                                        claimText.trim()
                                )
                                .queryParam(
                                        "languageCode",
                                        properties.getLanguageCode()
                                )
                                .queryParam(
                                        "pageSize",
                                        properties.getPageSize()
                                )
                                .build()
                        )
                        .header(
                                API_KEY_HEADER,
                                properties.getApiKey()
                        )
                        .retrieve()
                        .body(
                                GoogleFactCheckSearchResponse.class
                        );

        if (response == null) {
            log.info("[ClaimSift] Google returned an empty response body.");
            return new GoogleFactCheckSearchResponse();
        }

        logGoogleResponse(claimText, response);

        return response;
    }

    private void logGoogleResponse(
        String searchedClaim,
        GoogleFactCheckSearchResponse response) {

        List<GoogleClaimResponse> claims =
                response.getClaims();

        if (claims == null || claims.isEmpty()) {
            log.warn(
                    "[ClaimSift] Google returned no candidates for: {}",
                    searchedClaim
            );

            return;
        }

        log.warn(
                "[ClaimSift] Google returned {} candidates for: {}",
                claims.size(),
                searchedClaim
        );

        for (int index = 0; index < claims.size(); index++) {
            GoogleClaimResponse candidate = claims.get(index);

            String ratings =
                    candidate.getClaimReview() == null
                            ? "none"
                            : candidate.getClaimReview()
                                    .stream()
                                    .map(review -> review.getTextualRating())
                                    .toList()
                                    .toString();

            log.warn(
                    "[ClaimSift] Google candidate {}: '{}' | ratings={}",
                    index + 1,
                    candidate.getText(),
                    ratings
            );
        }
    }
}