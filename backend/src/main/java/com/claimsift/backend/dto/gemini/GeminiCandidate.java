package com.claimsift.backend.dto.gemini;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeminiCandidate {
    private GeminiContent content;
    private String finishReason;
}