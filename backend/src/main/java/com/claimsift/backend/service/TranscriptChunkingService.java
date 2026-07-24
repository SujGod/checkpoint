package com.claimsift.backend.service;

import com.claimsift.backend.constants.TranscriptConstants;
import com.claimsift.backend.dto.processing.TranscriptSegmentRequest;
import com.claimsift.backend.model.TranscriptChunk;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TranscriptChunkingService {
    public List<TranscriptChunk> chunkTranscript(String videoId, List<TranscriptSegmentRequest> inputSegments) {
        List<TranscriptSegmentRequest> segments = normalizeSegments(inputSegments);
        List<TranscriptChunk> chunks = new ArrayList<>();
        List<TranscriptSegmentRequest> currentSegments = new ArrayList<>();

        for (TranscriptSegmentRequest segment : segments) {
            if (CollectionUtils.isEmpty(currentSegments)) {
                currentSegments.add(segment);
                continue;
            }

            boolean exceedsTime = wouldExceedTimeLimit(currentSegments, segment);
            boolean exceedsCharacters = wouldExceedCharacterLimit(currentSegments, segment);

            if (exceedsTime || exceedsCharacters) {
                chunks.add(createChunk(videoId, currentSegments));
                currentSegments.clear();
            }

            currentSegments.add(segment);
        }

        if (!CollectionUtils.isEmpty(currentSegments)) {
            chunks.add(createChunk(videoId, currentSegments));
        }

        return chunks;
    }

    private List<TranscriptSegmentRequest> normalizeSegments(List<TranscriptSegmentRequest> inputSegments) {
        if (CollectionUtils.isEmpty(inputSegments)) {
            return List.of();
        }

        List<TranscriptSegmentRequest> sortedSegments = inputSegments.stream()
                .filter(this::hasUsableText)
                .sorted(Comparator.comparingDouble(TranscriptSegmentRequest::getStartSeconds))                
                .toList();

        List<TranscriptSegmentRequest> normalizedSegments = new ArrayList<>();

        for (int index = 0; index < sortedSegments.size(); index++) {
            TranscriptSegmentRequest current = sortedSegments.get(index);
            double duration = calculateDuration(sortedSegments, index);

            TranscriptSegmentRequest normalizedSegment = TranscriptSegmentRequest.builder()
                .text(normalizeText(current.getText()))
                .startSeconds(current.getStartSeconds())
                .durationSeconds(duration)
                .build();

            normalizedSegments.add(normalizedSegment);
        }

        return normalizedSegments;
    }

    private boolean hasUsableText(TranscriptSegmentRequest segment) {
        return Objects.nonNull(segment) && StringUtils.isNotBlank(segment.getText());
    }

    private String normalizeText(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private double calculateDuration(List<TranscriptSegmentRequest> segments, int index) {
        TranscriptSegmentRequest current = segments.get(index);

        if (index + 1 < segments.size()) {
            TranscriptSegmentRequest next = segments.get(index + 1);
            return Math.max(0.0, next.getStartSeconds() - current.getStartSeconds());
        }

        return Math.max(0.0,current.getDurationSeconds());
    }

    private boolean wouldExceedTimeLimit(List<TranscriptSegmentRequest> currentSegments, TranscriptSegmentRequest candidate) {
        TranscriptSegmentRequest first = currentSegments.get(0);

        double candidateEnd = candidate.getStartSeconds() + candidate.getDurationSeconds();
        double candidateDuration = candidateEnd - first.getStartSeconds();

        return candidateDuration > TranscriptConstants.MAX_CHUNK_SECONDS;
    }

    private boolean wouldExceedCharacterLimit(List<TranscriptSegmentRequest> currentSegments, TranscriptSegmentRequest candidate) {
        int currentCharacters = currentSegments.stream()
            .mapToInt(segment -> segment.getText().length())
            .sum();

        int separatingSpaces = currentSegments.size();
        int candidateCharacters = currentCharacters + separatingSpaces + candidate.getText().length();

        return candidateCharacters > TranscriptConstants.MAX_CHUNK_CHARACTERS;
    }

    private TranscriptChunk createChunk(String videoId, List<TranscriptSegmentRequest> segments) {
        TranscriptSegmentRequest first = segments.get(0);
        TranscriptSegmentRequest last = segments.get(segments.size() - 1);

        String text = segments.stream().map(TranscriptSegmentRequest::getText).collect(Collectors.joining(" "));

        double endSeconds = last.getStartSeconds() + last.getDurationSeconds();

        return TranscriptChunk.builder()
            .id(videoId + "-" + formatChunkIdTime(first.getStartSeconds()))
            .videoId(videoId)
            .startSeconds(first.getStartSeconds())
            .endSeconds(endSeconds)
            .segments(new ArrayList<>(segments))
            .build();
    }

    private String formatChunkIdTime(double startSeconds) {
        if (startSeconds == Math.floor(startSeconds)) {
            return Long.toString((long) startSeconds);
        }

        return Double.toString(startSeconds);
    }
}