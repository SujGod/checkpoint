package com.checkpoint.backend.dto;

import java.util.List;

import com.checkpoint.backend.model.Verdict;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactCheckResponse {
    
    private String claim;
    private Verdict verdict;
    private String explanation;
    private List<SourceResponse> sources;

}
