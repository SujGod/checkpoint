package com.claimsift.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.claimsift.backend.client.GeminiClient;
import com.claimsift.backend.config.GeminiGenerationConfig;
import com.claimsift.backend.config.properties.GeminiProperties;
import com.claimsift.backend.dto.claim.AiClaimExtractionResponse;
import com.claimsift.backend.dto.claim.AiExtractedClaimResponse;
import com.claimsift.backend.dto.claim.ExtractedClaimResponse;
import com.claimsift.backend.dto.gemini.GeminiCandidate;
import com.claimsift.backend.dto.gemini.GeminiContent;
import com.claimsift.backend.dto.gemini.GeminiGenerateContentRequest;
import com.claimsift.backend.dto.gemini.GeminiGenerateContentResponse;
import com.claimsift.backend.dto.gemini.GeminiPart;
import com.claimsift.backend.dto.processing.TranscriptSegmentRequest;
import com.claimsift.backend.model.TranscriptChunk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiClaimExtractionService {

    private static final String SYSTEM_PROMPT = """
            You extract standalone, externally verifiable factual claims
            from timestamped video transcripts, that can be properly fact-checked
            from Google Fact Check Tools API.

            A valid claim must:
            - make a specific factual assertion
            - identify its subject
            - make sense without surrounding conversation
            - be suitable for searching in a published fact-check database
            - preserve the speaker's intended meaning
            - be supported by the supplied transcript text

            Reject:
            - opinions
            - insults
            - rhetorical questions
            - predictions
            - jokes or sarcasm
            - conversational filler
            - incomplete sentence fragments
            - vague claims with unresolved pronouns
            - statements about feelings or beliefs
            - duplicate or semantically equivalent claims

            You may rewrite conversational wording into a concise,
            neutral, standalone claim only when the transcript provides
            enough context.

            Never invent missing people, organizations, policies,
            countries, dates, amounts, statistics, or events.

            startSeconds must equal the start of the earliest transcript
            segment supporting the claim.

            endSeconds must equal the end of the latest transcript segment
            supporting the claim.

            importanceScore must be between 0.0 and 1.0.

            Higher importance should be given to claims involving:
            - statistics or percentages
            - money or quantities
            - dates or historical events
            - government policies
            - public figures or organizations
            - scientific or medical assertions
            - claims with meaningful public impact
            """;

    private final GeminiClient geminiClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public List<ExtractedClaimResponse> extractClaims(TranscriptChunk chunk) {
        if (Objects.isNull(chunk) || CollectionUtils.isEmpty(chunk.getSegments())) {
            log.info("Chunk is null or has no segments: {}", chunk);
            return List.of();
        }

        
        log.info(
                "[ClaimSift] Sending chunk {} to Gemini: "
                        + "videoId={}, start={}, end={}",
                chunk.getId(),
                chunk.getVideoId(),
                chunk.getStartSeconds(),
                chunk.getEndSeconds()
        );
        GeminiGenerateContentRequest request = buildRequest(chunk);

        try {
            GeminiGenerateContentResponse response = geminiClient.generateContent(request);
            String responseJson = extractResponseText(response);
            AiClaimExtractionResponse extractionResponse = objectMapper.readValue(responseJson, AiClaimExtractionResponse.class);

            log.info("Response :: {} and chunk :: {}", extractionResponse, chunk);
            return validateAndMapClaims(extractionResponse, chunk);
        } catch (RestClientException exception) {
            log.error("[ClaimSift] Gemini request failed for chunk {}.", chunk.getId(), exception);
            return List.of();
        } catch (JsonProcessingException exception) {
            log.error("[ClaimSift] Could not parse Gemini response for chunk {}.", chunk.getId(), exception);
            return List.of();
        } catch (IllegalStateException exception) {
            log.error("[ClaimSift] Invalid Gemini response for chunk {}.", chunk.getId(), exception);
            return List.of();
        }
    }

    private GeminiGenerateContentRequest buildRequest(TranscriptChunk chunk) {

        return GeminiGenerateContentRequest.builder()
            .systemInstruction(buildSystemInstruction())
            .contents(List.of(buildUserContent(chunk)))
            .generationConfig(buildGenerationConfig())
            .build();
    }

    private GeminiContent buildSystemInstruction() {
        return GeminiContent.builder()
                .parts(List.of(buildTextPart(SYSTEM_PROMPT)))
                .build();
    }
    
    private GeminiContent buildUserContent(TranscriptChunk chunk) {
        return GeminiContent.builder()
                .role("user")
                .parts(List.of(buildTextPart(buildUserPrompt(chunk))))
                .build();
    }

    private GeminiPart buildTextPart(String text) {
        return GeminiPart.builder()
                .text(text)
                .build();
    }

    private GeminiGenerationConfig buildGenerationConfig() {
        return GeminiGenerationConfig.builder()
                .temperature(0.1)
                .maxOutputTokens(properties.getMaxOutputTokens())
                .responseMimeType("application/json")
                .responseJsonSchema(buildResponseSchema())
                .build();
    }

    private String buildUserPrompt(TranscriptChunk chunk) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("Extract no more than ");
        prompt.append(properties.getMaxClaimsPerChunk());
        prompt.append(" high-value standalone factual claims ");

        prompt.append("from this timestamped transcript chunk.\n\n");
        prompt.append("Video ID: ");
        prompt.append(chunk.getVideoId());
        prompt.append('\n');

        prompt.append("Chunk ID: ");
        prompt.append(chunk.getId());

        prompt.append("\n\nTranscript:\n");

        for (TranscriptSegmentRequest segment : chunk.getSegments()) {
            double segmentEnd = segment.getStartSeconds() + segment.getDurationSeconds();
            prompt.append("[%.2f-%.2f] %s%n".formatted(segment.getStartSeconds(), segmentEnd, segment.getText()));
        }

        return prompt.toString();
    }

    private Map<String, Object> buildResponseSchema() {
        Map<String, Object> claimSchema = Map.of(
            "type",
            "object",
            "properties",
            Map.of("text",
                Map.of(
                    "type",
                    "string",
                    "description",
                    "Concise standalone factual claim"
                ),
                "startSeconds",
                Map.of(
                    "type",
                    "number",
                    "description",
                    "Earliest supporting transcript timestamp"
                ),
                "endSeconds",
                Map.of(
                    "type",
                    "number",
                    "description",
                    "Latest supporting transcript timestamp"
                ),
                "importanceScore",
                Map.of(
                    "type",
                    "number",
                    "minimum",
                    0.0,
                    "maximum",
                    1.0
                )
            ),
            "required",
            List.of(
                "text",
                "startSeconds",
                "endSeconds",
                "importanceScore"
            ),
            "additionalProperties",
            false
        );

        return Map.of(
            "type",
            "object",
            "properties",
            Map.of(
                "claims",
                Map.of(
                        "type",
                        "array",
                        "maxItems",
                        properties.getMaxClaimsPerChunk(),
                        "items",
                        claimSchema
                )
            ),
            "required",
            List.of("claims"),
            "additionalProperties",
            false
        );
    }

    private String extractResponseText(GeminiGenerateContentResponse response) {

        if (CollectionUtils.isEmpty(response.getCandidates())) {
            throw new IllegalStateException("Gemini returned no candidates.");
        }

        GeminiCandidate firstCandidate = response.getCandidates().get(0);

        if (ObjectUtils.isEmpty(firstCandidate.getContent())) {
            throw new IllegalStateException("Gemini candidate contained no content.");
        }

        String text = firstCandidate.getContent()
            .getParts()
            .get(0)
            .getText();

        if (StringUtils.isBlank(text)) {
            throw new IllegalStateException("Gemini response text was blank.");
        }

        log.info("Gemini Response Text: {}", text);
        return text;
    }

    private List<ExtractedClaimResponse> validateAndMapClaims(AiClaimExtractionResponse response, TranscriptChunk chunk) {

        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getClaims())) {
            return List.of();
        }

        List<ExtractedClaimResponse> validClaims = new ArrayList<>();

        for (AiExtractedClaimResponse aiClaim : response.getClaims()) {

            if (!isValidClaim(aiClaim, chunk)) {
                log.debug(
                        "[ClaimSift] Rejected Gemini claim from chunk {}: {}",
                        chunk.getId(),
                        aiClaim == null
                                ? null
                                : aiClaim.getText()
                );

                continue;
            }

            validClaims.add(ExtractedClaimResponse.builder()
                .text(aiClaim.getText().trim())
                .startSeconds(aiClaim.getStartSeconds())
                .endSeconds(aiClaim.getEndSeconds())
                .importanceScore(aiClaim.getImportanceScore())
                .build()
            );
        }

        log.info("Valid claims found: {}", validClaims);

        return validClaims.stream()
            .limit(properties.getMaxClaimsPerChunk())
            .toList();
    }

    private boolean isValidClaim(AiExtractedClaimResponse claim, TranscriptChunk chunk) {

        if (Objects.isNull(claim) || StringUtils.isBlank(claim.getText())) {
            return false;
        }

        if (claim.getStartSeconds() < chunk.getStartSeconds()) {
            return false;
        }

        if (claim.getEndSeconds() > chunk.getEndSeconds()) {
            return false;
        }

        if (claim.getEndSeconds() <= claim.getStartSeconds()) {
            return false;
        }

        int wordCount = claim.getText().trim().split("\\s+").length;
        // need to see if word count should be moved to constant and 5-50 words per claim is good
        if (wordCount < 4 || wordCount > 50) {
            return false;
        }

        return claim.getImportanceScore() >= 0.0 && claim.getImportanceScore() <= 1.0;
    }
}