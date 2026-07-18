package com.claimsift.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtractedClaimResponse {
    private String text;
    private double startSeconds;
    private double endSeconds;
}