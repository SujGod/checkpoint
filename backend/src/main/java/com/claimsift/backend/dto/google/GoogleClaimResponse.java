package com.claimsift.backend.dto.google;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleClaimResponse {

    private String text;
    private String claimant;
    private String claimDate;
    private List<GoogleClaimReviewResponse> claimReview;
}