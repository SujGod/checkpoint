package com.claimsift.backend.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.claimsift.backend.constants.ClaimConstants;

@Service
public class ClaimNormalizationService {

    public String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return Arrays.stream(
                        text.toLowerCase(Locale.ROOT)
                                .replaceAll("[^a-z0-9\\s]", " ")
                                .trim()
                                .split("\\s+")
                )
                .filter(token -> !token.isBlank())
                .filter(token -> !ClaimConstants.STOP_WORDS.contains(token))
                .collect(Collectors.joining(" "));
    }

    public Set<String> tokenize(String text) {
        String normalized = normalize(text);

        if (normalized.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toSet());
    }
}