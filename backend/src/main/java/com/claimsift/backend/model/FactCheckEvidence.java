package com.claimsift.backend.model;

import java.util.List;

import com.claimsift.backend.dto.SourceResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactCheckEvidence {
    private Verdict verdict;
    private String explanation;
    private List<SourceResponse> sources;
}