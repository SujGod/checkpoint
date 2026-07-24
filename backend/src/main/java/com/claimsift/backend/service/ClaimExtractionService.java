package com.claimsift.backend.service;

import com.claimsift.backend.constants.ClaimConstants;
import com.claimsift.backend.dto.claim.ClaimExtractionRequest;
import com.claimsift.backend.dto.claim.ClaimExtractionResponse;
import com.claimsift.backend.dto.claim.ExtractedClaimResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ClaimExtractionService {

    public ClaimExtractionResponse extractClaims(ClaimExtractionRequest request) {

        String normalizedText = normalizeText(request.getText());

        if (normalizedText.isBlank()) {
            return buildResponse(request, List.of());
        }

        String[] sentences = ClaimConstants.SENTENCE_SPLIT_PATTERN.split(normalizedText);

        List<String> candidateClaims = new ArrayList<>();

        for (String sentence : sentences) {
            String candidate = cleanSentence(sentence);

            if (isPotentialFactualClaim(candidate)) {
                candidateClaims.add(candidate);
            }
        }

        List<ExtractedClaimResponse> claims = assignTimestamps(candidateClaims, request.getStartSeconds(), request.getEndSeconds());
        return buildResponse(request, claims);
    }

    private String normalizeText(String text) {
        return ClaimConstants.WHITESPACE_PATTERN.matcher(text.trim()).replaceAll(" ");
    }

    private String cleanSentence(String sentence) {
        String cleaned = sentence.trim();
        cleaned = cleaned.replaceAll("^(?i)(well|so|okay|ok|like|you know|basically),?\\s+","");
        return cleaned.trim();
    }

    private boolean isPotentialFactualClaim(String sentence) {
        if (StringUtils.isBlank(sentence)) {
            return false;
        }

        if (sentence.endsWith("?")) {
            return false;
        }

        String lowercase = sentence.toLowerCase(Locale.ROOT);

        if (startsWithAny(lowercase,ClaimConstants.OPINION_PREFIXES)) {
            return false;
        }

        int wordCount = countWords(sentence);

        if (wordCount < ClaimConstants.MINIMUM_WORD_COUNT || wordCount > ClaimConstants.MAXIMUM_WORD_COUNT) {
            return false;
        }

        if (looksLikeQuestion(lowercase)) {
            return false;
        }

        return containsNumber(sentence) || containsFactualSignal(lowercase) || containsProperNoun(sentence);
    }

    private boolean looksLikeQuestion(String sentence) {
        String firstWord = sentence.split("\\s+", 2)[0];
        return ClaimConstants.QUESTION_PREFIXES.contains(firstWord) && sentence.endsWith("?");
    }

    private boolean containsNumber(String sentence) {
        return ClaimConstants.NUMBER_PATTERN.matcher(sentence).matches();
    }

    private boolean containsFactualSignal(String sentence) {
        String[] words = sentence.split("\\W+");

        for (String word : words) {
            if (ClaimConstants.FACTUAL_SIGNAL_WORDS.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsProperNoun(String sentence) {
        String[] words = sentence.split("\\s+");

        for (int index = 1;
             index < words.length;
             index++) {

            String word = words[index].replaceAll("[^A-Za-z]", "");

            if (word.length() > 1 && Character.isUpperCase(word.charAt(0))) {
                return true;
            }
        }

        return false;
    }

    private int countWords(String sentence) {
        return sentence.trim().split("\\s+").length;
    }

    private boolean startsWithAny(String text, Set<String> prefixes) {
        return prefixes.stream().anyMatch(text::startsWith);
    }

    private List<ExtractedClaimResponse> assignTimestamps(List<String> claims, double chunkStart, double chunkEnd) {
        if (claims.isEmpty()) {
            return List.of();
        }

        double safeEnd = Math.max(chunkEnd, chunkStart);
        double duration = safeEnd - chunkStart;

        double timePerClaim = duration > 0 ? duration / claims.size() : 4.0;

        List<ExtractedClaimResponse> responses = new ArrayList<>();

        for (int index = 0; index < claims.size(); index++) {

            double start = chunkStart + index * timePerClaim;
            double end = index == claims.size() - 1 ? safeEnd : start + timePerClaim;

            responses.add(ExtractedClaimResponse
                            .builder()
                            .text(claims.get(index))
                            .startSeconds(start)
                            .endSeconds(end)
                            .build()
            );
        }

        return responses;
    }

    private ClaimExtractionResponse buildResponse(ClaimExtractionRequest request, List<ExtractedClaimResponse> claims) {

        return ClaimExtractionResponse
                .builder()
                .videoId(request.getVideoId())
                .chunkId(request.getChunkId())
                .claims(claims)
                .build();
    }
}