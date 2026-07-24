package com.claimsift.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

import com.claimsift.backend.dto.claim.ExtractedClaimResponse;
import com.claimsift.backend.dto.claim.FactCheckResponse;
import com.claimsift.backend.dto.processing.ProcessVideoRequest;
import com.claimsift.backend.dto.processing.ProcessVideoResponse;
import com.claimsift.backend.model.TranscriptChunk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingService {

    private final TranscriptChunkingService transcriptChunkingService;
    private final GeminiClaimExtractionService geminiClaimExtractionService;
    private final ClaimDeduplicationService claimDeduplicationService;
    private final ClaimPrioritizationService claimPrioritizationService;
    private final FactCheckService factCheckService;

    public ProcessVideoResponse processVideo(ProcessVideoRequest request) {

        List<TranscriptChunk> chunks = transcriptChunkingService.chunkTranscript(
                request.getVideoId(),
                request.getSegments()
        );

        log.info(
                "[ClaimSift] Video {} created {} transcript chunks.",
                request.getVideoId(),
                chunks.size()
        );

        List<ExtractedClaimResponse> extractedClaims = extractClaims(chunks);
        List<ExtractedClaimResponse> uniqueClaims = claimDeduplicationService.deduplicate(extractedClaims);
        List<ExtractedClaimResponse> selectedClaims = claimPrioritizationService.selectTopClaims(uniqueClaims);

        List<FactCheckResponse> factChecks = factCheckService.checkClaims(request.getVideoId(), selectedClaims);

        List<FactCheckResponse> sortedFactChecks = factChecks.stream()
                .sorted(Comparator.comparingDouble(FactCheckResponse::getStartSeconds))
                .toList();

        return ProcessVideoResponse.builder()
                .videoId(request.getVideoId())
                .factChecks(sortedFactChecks)
                .build();
    }

    private List<ExtractedClaimResponse> extractClaims(List<TranscriptChunk> chunks) {
        List<ExtractedClaimResponse> extractedClaims = new ArrayList<>();

        for (int index = 0; index < chunks.size(); index++) {
            TranscriptChunk chunk = chunks.get(index);

            log.info(
                    "[ClaimSift] Extracting claims from chunk {}/{}.",
                    index + 1,
                    chunks.size()
            );

            List<ExtractedClaimResponse> chunkClaims = geminiClaimExtractionService.extractClaims(chunk);
            extractedClaims.addAll(chunkClaims);
        }

        return extractedClaims;
    }
}