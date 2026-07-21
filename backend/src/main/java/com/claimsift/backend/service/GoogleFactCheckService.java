package com.claimsift.backend.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.claimsift.backend.dto.SourceResponse;
import com.claimsift.backend.config.GoogleFactCheckClient;
import com.claimsift.backend.dto.GoogleClaimResponse;
import com.claimsift.backend.dto.GoogleClaimReviewResponse;
import com.claimsift.backend.dto.GoogleFactCheckSearchResponse;
import com.claimsift.backend.model.FactCheckEvidence;
import com.claimsift.backend.model.GoogleFactCheckLookupResult;
import com.claimsift.backend.model.Verdict;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleFactCheckService {

    private final GoogleFactCheckClient googleFactCheckClient;
    private final ClaimNormalizationService claimNormalizationService;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(GoogleFactCheckService.class);

    @Value("${claimsift.fact-check.minimum-match-similarity:0.45}")
    private double minimumMatchSimilarity;

    public GoogleFactCheckLookupResult search(
            String extractedClaimText) {

        try {
            GoogleFactCheckSearchResponse response =
                    googleFactCheckClient.search(
                            extractedClaimText
                    );

            return findBestMatch(
                    extractedClaimText,
                    response
            );
        } catch (RestClientException exception) {
            return GoogleFactCheckLookupResult.error();
        }
    }

    private GoogleFactCheckLookupResult findBestMatch(
        String extractedClaimText,
        GoogleFactCheckSearchResponse response) {

        if (response == null
                || response.getClaims() == null
                || response.getClaims().isEmpty()) {

            LOGGER.info(
                    "[ClaimSift] No Google candidates returned for claim: {}",
                    extractedClaimText
            );

            return GoogleFactCheckLookupResult.noMatch();
        }

        GoogleClaimResponse bestClaim = null;
        double bestSimilarity = 0.0;

        for (GoogleClaimResponse candidate :
                response.getClaims()) {

            if (!hasReviews(candidate)) {
                continue;
            }

            double similarity =
                    calculateSimilarity(
                            extractedClaimText,
                            candidate.getText()
                    );

            LOGGER.info(
                    "[ClaimSift] Similarity {} between extracted claim '{}' "
                            + "and Google claim '{}'",
                    similarity,
                    extractedClaimText,
                    candidate.getText()
            );

            if (bestClaim == null
                    || similarity > bestSimilarity) {

                bestClaim = candidate;
                bestSimilarity = similarity;
            }
        }

        if (bestClaim == null) {
            LOGGER.info(
                    "[ClaimSift] Google candidates had no usable reviews."
            );

            return GoogleFactCheckLookupResult.noMatch();
        }

        FactCheckEvidence evidence =
                buildEvidence(bestClaim);

        return GoogleFactCheckLookupResult.match(
                evidence
        );
    }

    private boolean hasReviews(
            GoogleClaimResponse claim) {

        return claim != null
                && claim.getText() != null
                && claim.getClaimReview() != null
                && !claim.getClaimReview().isEmpty();
    }

    private FactCheckEvidence buildEvidence(
            GoogleClaimResponse matchedClaim) {

        List<GoogleClaimReviewResponse> reviews =
                matchedClaim.getClaimReview();

        GoogleClaimReviewResponse primaryReview =
                reviews.get(0);

        List<SourceResponse> sources =
                reviews.stream()
                        .map(this::mapSource)
                        .toList();

        return FactCheckEvidence.builder()
                .verdict(
                        mapVerdict(
                                primaryReview.getTextualRating()
                        )
                )
                .explanation(
                        buildExplanation(primaryReview)
                )
                .sources(sources)
                .build();
    }

    private SourceResponse mapSource(
            GoogleClaimReviewResponse review) {

        String publisher = null;

        if (review.getPublisher() != null) {
            publisher =
                    review.getPublisher().getName();
        }

        return SourceResponse.builder()
                .publisher(publisher)
                .title(review.getTitle())
                .url(review.getUrl())
                .rating(review.getTextualRating())
                .build();
    }

    private Verdict mapVerdict(
            String textualRating) {

        if (textualRating == null
                || textualRating.isBlank()) {

            return Verdict.INCONCLUSIVE;
        }

        String rating = textualRating
                .toLowerCase(Locale.ROOT);

        /*
         * Check mixed/uncertain ratings first.
         */
        if (containsAny(
                rating,
                "mixture",
                "mixed",
                "half true",
                "partly true",
                "partially true",
                "unproven",
                "unverified",
                "missing context",
                "needs context",
                "out of context",
                "satire"
        )) {
            return Verdict.INCONCLUSIVE;
        }

        /*
         * Check negative ratings before positive ratings.
         */
        if (containsAny(
                rating,
                "false",
                "incorrect",
                "inaccurate",
                "fake",
                "misleading",
                "pants on fire",
                "not true",
                "unsupported"
        )) {
            return Verdict.FALSE;
        }

        if (containsAny(
                rating,
                "true",
                "correct",
                "accurate",
                "verified"
        )) {
            return Verdict.TRUE;
        }

        return Verdict.INCONCLUSIVE;
    }

    private boolean containsAny(
            String text,
            String... values) {

        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }

    private String buildExplanation(
            GoogleClaimReviewResponse review) {

        String publisher =
                "A fact-checking publisher";

        if (review.getPublisher() != null
                && review.getPublisher().getName() != null
                && !review.getPublisher()
                        .getName()
                        .isBlank()) {

            publisher =
                    review.getPublisher().getName();
        }

        String rating =
                review.getTextualRating() == null
                        ? "an unspecified rating"
                        : review.getTextualRating();

        return publisher
                + " reviewed a matching published claim and rated it \""
                + rating
                + "\".";
    }

    private double calculateSimilarity(
            String firstText,
            String secondText) {

        Set<String> firstTokens =
                claimNormalizationService.tokenize(
                        firstText
                );

        Set<String> secondTokens =
                claimNormalizationService.tokenize(
                        secondText
                );

        if (firstTokens.isEmpty()
                || secondTokens.isEmpty()) {

            return 0.0;
        }

        Set<String> intersection =
                new HashSet<>(firstTokens);

        intersection.retainAll(secondTokens);

        Set<String> union =
                new HashSet<>(firstTokens);

        union.addAll(secondTokens);

        return (double) intersection.size()
                / union.size();
    }
}