package com.claimsift.backend.dto.gemini;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiGenerateContentResponse {
    private List<GeminiCandidate> candidates;
}