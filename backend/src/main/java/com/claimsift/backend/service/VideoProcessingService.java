package com.claimsift.backend.service;

import java.util.ArrayList;
import java.util.List;

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

    private final TranscriptChunkingService transcriptChunkingService;
    private final ClaimExtractionService claimExtractionService;
    private final ClaimDeduplicationService claimDeduplicationService;
    private final FactCheckService factCheckService;

    public ProcessVideoResponse processVideo(
        ProcessVideoRequest request
    ) {
        List<TranscriptChunk> chunks =
            transcriptChunkingService.chunkTranscript(
                request.getVideoId(),
                request.getSegments()
            );

        List<ExtractedClaimResponse> extractedClaims =
            extractClaims(chunks);

        List<ExtractedClaimResponse> uniqueClaims =
            claimDeduplicationService.deduplicate(
                extractedClaims
            );

        List<FactCheckResponse> factChecks =
            uniqueClaims.stream()
                .map(factCheckService::checkClaim)
                .toList();

        return ProcessVideoResponse.builder()
            .videoId(request.getVideoId())
            .factChecks(factChecks)
            .build();
    }

    private List<ExtractedClaimResponse> extractClaims(
        List<TranscriptChunk> chunks
    ) {
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

            ClaimExtractionResponse extractionResponse =
                claimExtractionService.extractClaims(
                    extractionRequest
                );

            if (
                extractionResponse.getClaims() != null
                && !extractionResponse.getClaims().isEmpty()
            ) {
                extractedClaims.addAll(
                    extractionResponse.getClaims()
                );
            }
        }

        return extractedClaims;
    }
}