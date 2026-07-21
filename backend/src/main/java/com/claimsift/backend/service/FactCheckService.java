package com.claimsift.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.claimsift.backend.dto.ExtractedClaimResponse;
import com.claimsift.backend.dto.FactCheckResponse;
import com.claimsift.backend.model.CachedFactCheckResult;
import com.claimsift.backend.model.FactCheckEvidence;
import com.claimsift.backend.model
        .GoogleFactCheckLookupResult;
import com.claimsift.backend.model
        .GoogleFactCheckLookupStatus;
import com.claimsift.backend.model.Verdict;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class FactCheckService {

    private final GoogleFactCheckService googleFactCheckService;
    private final ClaimNormalizationService claimNormalizationService;
    private final Cache<String, CachedFactCheckResult>
            factCheckResultCache;
    private final int maxGoogleQueriesPerVideo;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(FactCheckService.class);

    public FactCheckService(
            GoogleFactCheckService googleFactCheckService,
            ClaimNormalizationService claimNormalizationService,
            Cache<String, CachedFactCheckResult>
                    factCheckResultCache,
            @Value(
                "${claimsift.fact-check.max-google-queries-per-video:15}"
            )
            int maxGoogleQueriesPerVideo) {

        this.googleFactCheckService =
                googleFactCheckService;

        this.claimNormalizationService =
                claimNormalizationService;

        this.factCheckResultCache =
                factCheckResultCache;

        this.maxGoogleQueriesPerVideo =
                maxGoogleQueriesPerVideo;
    }

public List<FactCheckResponse> checkClaims(
        String videoId,
        List<ExtractedClaimResponse> claims) {

    if (claims == null || claims.isEmpty()) {
        return List.of();
    }

    int maximumGoogleQueries = maxGoogleQueriesPerVideo;

    int googleQueriesUsed = 0;
    int cacheHits = 0;
    int skippedClaims = 0;

    List<FactCheckResponse> responses =
            new ArrayList<>();

    List<String> googleSearchedClaims =
            new ArrayList<>();

    LOGGER.info(
            "[ClaimSift] Fact-checking video {}: "
                    + "{} candidate claims, Google limit {}",
            videoId,
            claims.size(),
            maximumGoogleQueries
    );

    for (ExtractedClaimResponse claim : claims) {
        String cacheKey =
                claimNormalizationService.normalize(
                        claim.getText()
                );

        if (cacheKey.isBlank()) {
            continue;
        }

        CachedFactCheckResult cachedResult =
                factCheckResultCache.getIfPresent(
                        cacheKey
                );

        if (cachedResult != null) {
            cacheHits++;

            responses.add(
                    buildFromCachedResult(
                            videoId,
                            claim,
                            cachedResult
                    )
            );

            continue;
        }

        if (googleQueriesUsed >= maximumGoogleQueries) {
            skippedClaims++;

            /*
             * Do not log every skipped claim.
             */
            responses.add(
                    buildInconclusiveResponse(
                            videoId,
                            claim,
                            "The per-video published fact-check search limit was reached."
                    )
            );

            continue;
        }

        googleQueriesUsed++;
        googleSearchedClaims.add(claim.getText());

        LOGGER.warn(
                "\n"
                        + "==================================================\n"
                        + "[ClaimSift] GOOGLE SEARCH {}/{}\n"
                        + "Claim: {}\n"
                        + "Time: {}-{} seconds\n"
                        + "==================================================",
                googleQueriesUsed,
                maximumGoogleQueries,
                claim.getText(),
                claim.getStartSeconds(),
                claim.getEndSeconds()
        );

        GoogleFactCheckLookupResult lookupResult =
                googleFactCheckService.search(
                        claim.getText()
                );

        LOGGER.warn(
                "[ClaimSift] GOOGLE RESULT {}/{}: {}",
                googleQueriesUsed,
                maximumGoogleQueries,
                lookupResult.getStatus()
        );

        responses.add(
                handleLookupResult(
                        videoId,
                        claim,
                        cacheKey,
                        lookupResult
                )
        );
    }

    logFactCheckSummary(
            videoId,
            claims.size(),
            googleSearchedClaims,
            cacheHits,
            skippedClaims
    );

    return responses;
}

private void logFactCheckSummary(
        String videoId,
        int totalClaims,
        List<String> searchedClaims,
        int cacheHits,
        int skippedClaims) {

    LOGGER.warn(
            "\n"
                    + "==================================================\n"
                    + "[ClaimSift] VIDEO FACT-CHECK SUMMARY\n"
                    + "Video: {}\n"
                    + "Candidate claims: {}\n"
                    + "Google searches: {}\n"
                    + "Cache hits: {}\n"
                    + "Skipped after limit: {}\n"
                    + "==================================================",
            videoId,
            totalClaims,
            searchedClaims.size(),
            cacheHits,
            skippedClaims
    );

    for (int index = 0;
            index < searchedClaims.size();
            index++) {

        LOGGER.warn(
                "[ClaimSift] Searched claim {}/{}: {}",
                index + 1,
                searchedClaims.size(),
                searchedClaims.get(index)
        );
    }
}

    private FactCheckResponse handleLookupResult(
            String videoId,
            ExtractedClaimResponse claim,
            String cacheKey,
            GoogleFactCheckLookupResult lookupResult) {

        if (lookupResult.getStatus()
                == GoogleFactCheckLookupStatus.MATCH) {

            CachedFactCheckResult cachedResult =
                    CachedFactCheckResult.match(
                            lookupResult.getEvidence()
                    );

            factCheckResultCache.put(
                    cacheKey,
                    cachedResult
            );

            return buildFromEvidence(
                    videoId,
                    claim,
                    lookupResult.getEvidence()
            );
        }

        if (lookupResult.getStatus()
                == GoogleFactCheckLookupStatus.NO_MATCH) {

            factCheckResultCache.put(
                    cacheKey,
                    CachedFactCheckResult.noMatch()
            );

            return buildInconclusiveResponse(
                    videoId,
                    claim,
                    "No matching published fact check was found."
            );
        }

        /*
         * API errors are intentionally not cached.
         */
        return buildInconclusiveResponse(
                videoId,
                claim,
                "The published fact-check service was temporarily unavailable."
        );
    }

    private FactCheckResponse buildFromCachedResult(
            String videoId,
            ExtractedClaimResponse claim,
            CachedFactCheckResult cachedResult) {

        if (!cachedResult.isMatchFound()
                || cachedResult.getEvidence() == null) {

            return buildInconclusiveResponse(
                    videoId,
                    claim,
                    "No matching published fact check was found."
            );
        }

        return buildFromEvidence(
                videoId,
                claim,
                cachedResult.getEvidence()
        );
    }

    private FactCheckResponse buildFromEvidence(
            String videoId,
            ExtractedClaimResponse claim,
            FactCheckEvidence evidence) {

        return FactCheckResponse.builder()
                .id(createFactCheckId(videoId, claim))
                .claim(claim.getText())
                .startSeconds(
                        claim.getStartSeconds()
                )
                .endSeconds(
                        claim.getEndSeconds()
                )
                .verdict(evidence.getVerdict())
                .explanation(
                        evidence.getExplanation()
                )
                .sources(
                        evidence.getSources() == null
                                ? List.of()
                                : evidence.getSources()
                )
                .build();
    }

    private FactCheckResponse buildInconclusiveResponse(
            String videoId,
            ExtractedClaimResponse claim,
            String explanation) {

        return FactCheckResponse.builder()
                .id(createFactCheckId(videoId, claim))
                .claim(claim.getText())
                .startSeconds(
                        claim.getStartSeconds()
                )
                .endSeconds(
                        claim.getEndSeconds()
                )
                .verdict(Verdict.INCONCLUSIVE)
                .explanation(explanation)
                .sources(List.of())
                .build();
    }

    private String createFactCheckId(
            String videoId,
            ExtractedClaimResponse claim) {

        String source =
                videoId
                        + ":"
                        + claim.getStartSeconds()
                        + ":"
                        + claim.getEndSeconds()
                        + ":"
                        + claim.getText();

        return UUID.nameUUIDFromBytes(
                source.getBytes(
                        StandardCharsets.UTF_8
                )
        ).toString();
    }
}