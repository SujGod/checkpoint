package com.checkpoint.backend.controller;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkpoint.backend.dto.FactCheckRequest;
import com.checkpoint.backend.dto.FactCheckResponse;
import com.checkpoint.backend.model.Verdict;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/facts")
public class FactCheckController {
    
    @GetMapping("/check")
    public FactCheckResponse factCheck(@Valid @RequestBody FactCheckRequest request) {
        
        return new FactCheckResponse(request.getClaim(), Verdict.TRUE, "Test explanation", new ArrayList<>());
    }
}
