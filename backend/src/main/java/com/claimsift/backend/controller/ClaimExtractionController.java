package com.claimsift.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.claimsift.backend.dto.claim.ClaimExtractionRequest;
import com.claimsift.backend.dto.claim.ClaimExtractionResponse;
import com.claimsift.backend.service.ClaimExtractionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimExtractionController {

    private final ClaimExtractionService service;

    public ClaimExtractionController(ClaimExtractionService service) {
        this.service = service;
    }

    @PostMapping("/extract")
    public ClaimExtractionResponse extractClaims(@Valid @RequestBody ClaimExtractionRequest request) {
        return service.extractClaims(request);
    }
}