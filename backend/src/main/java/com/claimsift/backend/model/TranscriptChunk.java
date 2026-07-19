package com.claimsift.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptChunk {
    String id;
    String videoId;
    String text;
    double startSeconds;
    double endSeconds;
}