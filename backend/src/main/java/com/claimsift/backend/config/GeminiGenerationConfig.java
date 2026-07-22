package com.claimsift.backend.config;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeminiGenerationConfig {
    private Double temperature;
    private Integer maxOutputTokens;
    private String responseMimeType;
    private Map<String, Object> responseJsonSchema;
}