package com.claimsift.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.claimsift.backend.dto.ExtractedClaimResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimDeduplicationService {

    private final ClaimNormalizationService claimNormalizationService;

    public List<ExtractedClaimResponse> deduplicate(List<ExtractedClaimResponse> claims) {
        if (CollectionUtils.isEmpty(claims)) {
            return List.of();
        }

        List<ExtractedClaimResponse> sortedClaims =
                claims.stream()
                        .filter(this::isValidClaim)
                        .sorted(Comparator.comparingDouble(ExtractedClaimResponse::getStartSeconds))
                        .toList();

        Set<String> seenClaims = new HashSet<>();
        List<ExtractedClaimResponse> uniqueClaims = new ArrayList<>();

        for (ExtractedClaimResponse claim : sortedClaims) {
            String normalizedClaim = claimNormalizationService.normalize(claim.getText());

            if (normalizedClaim.isBlank()) {
                continue;
            }

            if (seenClaims.add(normalizedClaim)) {
                uniqueClaims.add(claim);
            }
        }

        return uniqueClaims;
    }

    private boolean isValidClaim(ExtractedClaimResponse claim) {
        return claim != null && claim.getText() != null && !claim.getText().isBlank();
    }
}