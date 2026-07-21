package com.claimsift.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.claimsift.backend.dto.ClaimExtractionRequest;
import com.claimsift.backend.dto.ClaimExtractionResponse;
import com.claimsift.backend.dto.ExtractedClaimResponse;
import com.claimsift.backend.dto.FactCheckResponse;
import com.claimsift.backend.dto.ProcessVideoRequest;
import com.claimsift.backend.dto.ProcessVideoResponse;
import com.claimsift.backend.model.TranscriptChunk;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(VideoProcessingService.class);

    private final TranscriptChunkingService transcriptChunkingService;
    private final ClaimExtractionService claimExtractionService;
    private final ClaimDeduplicationService claimDeduplicationService;
    private final FactCheckService factCheckService;

    public ProcessVideoResponse processVideo(ProcessVideoRequest request) {

        List<TranscriptChunk> chunks = transcriptChunkingService.chunkTranscript(request.getVideoId(), request.getSegments());

        List<ExtractedClaimResponse> extractedClaims =
                extractClaims(chunks);

        LOGGER.info(
                "[ClaimSift] Extracted {} claims for video {}",
                extractedClaims.size(),
                request.getVideoId()
        );

        List<ExtractedClaimResponse> uniqueClaims =
                claimDeduplicationService.deduplicate(
                        extractedClaims
                );

        LOGGER.info(
                "[ClaimSift] {} unique claims remain after deduplication",
                uniqueClaims.size()
        );


        List<FactCheckResponse> factChecks =
                factCheckService.checkClaims(
                        request.getVideoId(),
                        uniqueClaims
                );

        return ProcessVideoResponse.builder()
                .videoId(request.getVideoId())
                .factChecks(factChecks)
                .build();
    }

    private void logClaims(
            String category,
            List<ExtractedClaimResponse> claims) {

        for (int index = 0; index < claims.size(); index++) {
            ExtractedClaimResponse claim = claims.get(index);

            LOGGER.info(
                    "[ClaimSift] {} claim {}: [{}-{}] {}",
                    category,
                    index + 1,
                    claim.getStartSeconds(),
                    claim.getEndSeconds(),
                    claim.getText()
            );
        }
    }

    private List<ExtractedClaimResponse> extractClaims(
            List<TranscriptChunk> chunks) {

        List<ExtractedClaimResponse> extractedClaims =
                new ArrayList<>();

        for (TranscriptChunk chunk : chunks) {
            ClaimExtractionRequest extractionRequest =
                    ClaimExtractionRequest.builder()
                            .videoId(chunk.getVideoId())
                            .chunkId(chunk.getId())
                            .text(chunk.getText())
                            .startSeconds(chunk.getStartSeconds())
                            .endSeconds(chunk.getEndSeconds())
                            .build();

            ClaimExtractionResponse response =
                    claimExtractionService.extractClaims(
                            extractionRequest
                    );

            if (response != null
                    && response.getClaims() != null) {

                extractedClaims.addAll(response.getClaims());
            }
        }

        return extractedClaims;
    }
}