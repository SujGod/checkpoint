package com.claimsift.backend.dto.gemini;

import java.util.List;

import com.claimsift.backend.config.GeminiGenerationConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeminiGenerateContentRequest {
    private GeminiContent systemInstruction;
    private List<GeminiContent> contents;
    private GeminiGenerationConfig generationConfig;
}