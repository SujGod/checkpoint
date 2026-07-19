package com.claimsift.backend.dto;

import java.util.List;

import com.claimsift.backend.model.Verdict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FactCheckResponse {
    
    private String id;
    private String claim;
    private double startSeconds;
    private double endSeconds;
    private Verdict verdict;
    private String explanation;
    private List<SourceResponse> sources;

}
