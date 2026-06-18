package com.summai.api.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Gemini gemini = new Gemini();

    public Cors getCors() {
        return cors;
    }

    public Gemini getGemini() {
        return gemini;
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Gemini {
        private String apiKey;
        private String model = "gemini-1.5-flash";
        private int timeoutSeconds = 30;
        private int maxTranscriptCharacters = 45000;
        private String systemPrompt;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public int getMaxTranscriptCharacters() {
            return maxTranscriptCharacters;
        }

        public void setMaxTranscriptCharacters(int maxTranscriptCharacters) {
            this.maxTranscriptCharacters = maxTranscriptCharacters;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }
    }
}
