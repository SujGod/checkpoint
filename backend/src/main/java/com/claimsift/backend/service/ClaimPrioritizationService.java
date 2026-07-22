package com.claimsift.backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.claimsift.backend.config.properties.ClaimSiftFactCheckProperties;
import com.claimsift.backend.dto.ExtractedClaimResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimPrioritizationService {

    private final ClaimSiftFactCheckProperties properties;

    public List<ExtractedClaimResponse> selectTopClaims(
            List<ExtractedClaimResponse> claims) {

        if (claims == null || claims.isEmpty()) {
            return List.of();
        }

        return claims.stream()
                .filter(this::isValid)
                .sorted(
                        Comparator.comparingDouble(
                                ExtractedClaimResponse::
                                        getImportanceScore
                        ).reversed()
                )
                .limit(
                        properties.getMaxClaimsPerVideo()
                )
                .toList();
    }

    private boolean isValid(
            ExtractedClaimResponse claim) {

        return claim != null
                && claim.getText() != null
                && !claim.getText().isBlank();
    }
}
