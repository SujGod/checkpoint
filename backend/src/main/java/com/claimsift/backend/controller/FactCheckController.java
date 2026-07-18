package com.claimsift.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.claimsift.backend.dto.FactCheckRequest;
import com.claimsift.backend.dto.FactCheckResponse;
import com.claimsift.backend.model.Verdict;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class FactCheckController {
    
    @PostMapping("/checks")
    public FactCheckResponse getFactCheck(@Valid @RequestBody FactCheckRequest request) {
        
        return new FactCheckResponse(request.getClaim(), Verdict.TRUE, "Test explanation", List.of());
    }
}
