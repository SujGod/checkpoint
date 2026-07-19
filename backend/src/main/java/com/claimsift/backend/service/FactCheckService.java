package com.claimsift.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.claimsift.backend.dto.ExtractedClaimResponse;
import com.claimsift.backend.dto.FactCheckResponse;
import com.claimsift.backend.model.Verdict;

@Service
public class FactCheckService {

    public FactCheckResponse checkClaim(
            ExtractedClaimResponse claim) {

        return FactCheckResponse.builder()
                .id(createFactCheckId(claim))
                .claim(claim.getText())
                .startSeconds(claim.getStartSeconds())
                .endSeconds(claim.getEndSeconds())
                .verdict(Verdict.INCONCLUSIVE)
                .explanation(
                        "No matching fact-check evidence has been evaluated yet."
                )
                .sources(List.of())
                .build();
    }

    private String createFactCheckId(
            ExtractedClaimResponse claim) {

        String source = claim.getStartSeconds()
                + ":"
                + claim.getEndSeconds()
                + ":"
                + claim.getText();

        return UUID.nameUUIDFromBytes(
                source.getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}