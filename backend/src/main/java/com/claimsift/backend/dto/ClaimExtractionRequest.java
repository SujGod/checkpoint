package com.claimsift.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimExtractionRequest {
    @NotBlank
    private String videoId;
    @NotBlank
    private String chunkId;
    @NotBlank
    private String text;
    private double startSeconds;
    private double endSeconds;
}
