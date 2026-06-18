package com.summai.api.controller;

import com.summai.api.dto.SummarizeRequest;
import com.summai.api.dto.SummarizeResponse;
import com.summai.api.service.YouTubeSummarizationService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SummarizeController {

    private final YouTubeSummarizationService summarizationService;

    public SummarizeController(YouTubeSummarizationService summarizationService) {
        this.summarizationService = summarizationService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarize(@Valid @RequestBody SummarizeRequest request) throws Exception {
        String summary = summarizationService.summarize(request.getVideoUrl());
        return ResponseEntity.ok(new SummarizeResponse(summary));
    }
}
