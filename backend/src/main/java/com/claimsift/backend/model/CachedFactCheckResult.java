package com.claimsift.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CachedFactCheckResult {

    private boolean matchFound;
    private FactCheckEvidence evidence;

    public static CachedFactCheckResult match(FactCheckEvidence evidence) {
        
        return CachedFactCheckResult.builder()
                .matchFound(true)
                .evidence(evidence)
                .build();
    }

    public static CachedFactCheckResult noMatch() {
       
        return CachedFactCheckResult.builder()
                .matchFound(false)
                .evidence(null)
                .build();
    }
}