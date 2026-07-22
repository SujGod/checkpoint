package com.claimsift.backend.dto.claim;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClaimExtractionResponse {
    private List<AiExtractedClaimResponse> claims;
}