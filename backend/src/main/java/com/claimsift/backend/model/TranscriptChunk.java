package com.claimsift.backend.model;

import java.util.List;
import com.claimsift.backend.dto.TranscriptSegmentRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptChunk {
    private String id;
    private String videoId;
    private String text;
    private double startSeconds;
    private double endSeconds;
    private List<TranscriptSegmentRequest> segments;
}