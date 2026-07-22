package com.claimsift.backend.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiExtractedClaimResponse {
    private String text;
    private double startSeconds;
    private double endSeconds;
    private double importanceScore;
}