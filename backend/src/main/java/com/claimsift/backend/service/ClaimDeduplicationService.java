package com.claimsift.backend.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.claimsift.backend.dto.claim.ExtractedClaimResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimDeduplicationService {

    private final ClaimNormalizationService claimNormalizationService;

    public List<ExtractedClaimResponse> deduplicate(List<ExtractedClaimResponse> claims) {

        if (CollectionUtils.isEmpty(claims)) {
            return List.of();
        }

        Map<String, ExtractedClaimResponse> uniqueClaims = new LinkedHashMap<>();

        for (ExtractedClaimResponse claim : claims) {
            if (!isValidClaim(claim)) {
                continue;
            }

            String normalizedClaim = claimNormalizationService.normalize(claim.getText());

            if (normalizedClaim.isBlank()) {
                continue;
            }

            uniqueClaims.merge(normalizedClaim, claim, this::selectBetterClaim);
        }

        return uniqueClaims.values()
            .stream()
            .sorted(Comparator.comparingDouble(ExtractedClaimResponse::getStartSeconds))
            .toList();
    }

    private ExtractedClaimResponse selectBetterClaim(ExtractedClaimResponse first, ExtractedClaimResponse second) {
        if (second.getImportanceScore() > first.getImportanceScore()) {
            return second;
        }

        return first;
    }

    private boolean isValidClaim(ExtractedClaimResponse claim) {
        return Objects.nonNull(claim) && StringUtils.isNotBlank(claim.getText());
    }
}