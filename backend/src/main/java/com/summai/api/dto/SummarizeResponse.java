package com.summai.api.dto;

public class SummarizeResponse {

    private final String summary;

    public SummarizeResponse(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }
}
