package com.claimsift.backend.dto.processing;

import java.util.List;

import com.claimsift.backend.dto.claim.FactCheckResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessVideoResponse {
    private String videoId;
    private List<FactCheckResponse> factChecks;
}