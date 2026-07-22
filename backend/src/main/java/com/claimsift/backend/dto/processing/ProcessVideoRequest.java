package com.claimsift.backend.dto.processing;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessVideoRequest {
    @NotBlank
    private String videoId;

    @Valid
    @NotEmpty
    private List< TranscriptSegmentRequest> segments;
}