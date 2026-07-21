package com.claimsift.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleClaimReviewResponse {

    private GooglePublisherResponse publisher;
    private String url;
    private String title;
    private String reviewDate;
    private String textualRating;
    private String languageCode;
}