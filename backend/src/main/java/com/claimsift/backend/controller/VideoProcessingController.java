package com.claimsift.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.claimsift.backend.dto.ProcessVideoRequest;
import com.claimsift.backend.dto.ProcessVideoResponse;
import com.claimsift.backend.service.VideoProcessingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoProcessingController {

    private final VideoProcessingService videoProcessingService;

    @PostMapping("/process")
    public ResponseEntity<ProcessVideoResponse> processVideo(
        @Valid @RequestBody ProcessVideoRequest request
    ) {
        ProcessVideoResponse response =
            videoProcessingService.processVideo(request);

        return ResponseEntity.ok(response);
    }
}