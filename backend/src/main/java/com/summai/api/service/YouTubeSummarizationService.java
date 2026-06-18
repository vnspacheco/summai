package com.summai.api.service;

import com.summai.api.util.YouTubeUrlUtils;
import org.springframework.stereotype.Service;

@Service
public class YouTubeSummarizationService {

    private final YouTubeTranscriptService transcriptService;
    private final GeminiService geminiService;

    public YouTubeSummarizationService(YouTubeTranscriptService transcriptService, GeminiService geminiService) {
        this.transcriptService = transcriptService;
        this.geminiService = geminiService;
    }

    public String summarize(String videoUrl) {
        String videoId = YouTubeUrlUtils.extractVideoId(videoUrl);
        String transcript = transcriptService.fetchTranscript(videoId);
        return geminiService.summarize(transcript);
    }
}
