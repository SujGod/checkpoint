package com.claimsift.backend.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClaimAnalysis {
    private boolean accepted;
    private double score;
    private String rejectionReason;
    private boolean hasSubject;
    private boolean hasPredicate;
    private boolean hasEntity;
    private boolean hasQuantity;
    private boolean containsHedge;
    private boolean containsOpinion;
}