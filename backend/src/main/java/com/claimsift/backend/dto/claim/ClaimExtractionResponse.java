package com.claimsift.backend.dto.claim;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimExtractionResponse {
    private String videoId;
    private String chunkId;
    private List<ExtractedClaimResponse> claims;
}