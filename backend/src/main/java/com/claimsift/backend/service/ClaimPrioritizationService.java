package com.claimsift.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.claimsift.backend.config.properties.ClaimSiftFactCheckProperties;
import com.claimsift.backend.dto.claim.ExtractedClaimResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimPrioritizationService {

    private final ClaimSiftFactCheckProperties properties;

    public List<ExtractedClaimResponse> selectTopClaims(List<ExtractedClaimResponse> claims) {

        if (CollectionUtils.isEmpty(claims)) {
            return List.of();
        }

        return claims.stream()
            .filter(this::isValid)
            .sorted(Comparator.comparingDouble(ExtractedClaimResponse::getImportanceScore).reversed())
            .limit(properties.getMaxClaimsPerVideo())
            .toList();
    }

    private boolean isValid(ExtractedClaimResponse claim) {
        return Objects.nonNull(claim) && StringUtils.isNotBlank(claim.getText());
    }
}
