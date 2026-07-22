package com.claimsift.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.claimsift.backend.client.GoogleFactCheckClient;
import com.claimsift.backend.dto.google.GoogleFactCheckSearchResponse;
import com.claimsift.backend.dto.processing.ProcessVideoRequest;
import com.claimsift.backend.dto.processing.ProcessVideoResponse;
import com.claimsift.backend.service.VideoProcessingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoProcessingController {

    private final VideoProcessingService videoProcessingService;
    private final GoogleFactCheckClient googleFactCheckClient;

    @PostMapping("/process")
    public ResponseEntity<ProcessVideoResponse> processVideo(@Valid @RequestBody ProcessVideoRequest request) {
        ProcessVideoResponse response = videoProcessingService.processVideo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<GoogleFactCheckSearchResponse> search(@RequestParam String query) {
        GoogleFactCheckSearchResponse response = googleFactCheckClient.search(query);
        return ResponseEntity.ok(response);
    }
}