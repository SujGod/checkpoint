package com.claimsift.backend.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceResponse {

    private String publisher;
    private String title;
    private String url;
    private String rating;
}