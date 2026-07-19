package com.claimsift.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.claimsift.backend.dto.ExtractedClaimResponse;

@Service
public class ClaimDeduplicationService {

    public List<ExtractedClaimResponse> deduplicate(
            List<ExtractedClaimResponse> claims) {

        if (claims == null || claims.isEmpty()) {
            return List.of();
        }

        List<ExtractedClaimResponse> sortedClaims = claims.stream()
                .filter(this::isValidClaim)
                .sorted(
                        Comparator.comparingDouble(
                                ExtractedClaimResponse::getStartSeconds
                        )
                )
                .toList();

        Set<String> seenClaims = new HashSet<>();
        List<ExtractedClaimResponse> uniqueClaims = new ArrayList<>();

        for (ExtractedClaimResponse claim : sortedClaims) {
            String normalizedText =
                    normalizeClaimText(claim.getText());

            if (seenClaims.add(normalizedText)) {
                uniqueClaims.add(claim);
            }
        }

        return uniqueClaims;
    }

    private boolean isValidClaim(
            ExtractedClaimResponse claim) {

        return claim != null
                && claim.getText() != null
                && !claim.getText().isBlank();
    }

    private String normalizeClaimText(String text) {
        return text
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}