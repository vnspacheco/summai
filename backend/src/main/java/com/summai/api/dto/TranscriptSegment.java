package com.summai.api.dto;

public class TranscriptSegment {
    String text;
    double start;
    double duration;

    public TranscriptSegment(String text, double start, double duration) {
        this.text = text;
        this.start = start;
        this.duration = duration;
    }
}
