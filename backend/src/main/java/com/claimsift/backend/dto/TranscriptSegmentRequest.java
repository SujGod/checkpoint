package com.claimsift.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TranscriptSegmentRequest {
    @NotBlank
    private String text;

    @PositiveOrZero
    private double startSeconds;

    @PositiveOrZero
    private double durationSeconds;
}