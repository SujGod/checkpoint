package com.checkpoint.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.backend.dto.FactCheckRequest;
import com.checkpoint.backend.dto.FactCheckResponse;
import com.checkpoint.backend.model.Verdict;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class FactCheckController {
    
    @PostMapping("/checks")
    public FactCheckResponse getFactCheck(@Valid @RequestBody FactCheckRequest request) {
        
        return new FactCheckResponse(request.getClaim(), Verdict.TRUE, "Test explanation", List.of());
    }
}
