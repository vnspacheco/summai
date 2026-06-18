package com.summai.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.summai.api.config.AppProperties;
import com.summai.api.exception.ApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiService(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(appProperties.getGemini().getTimeoutSeconds()))
                .build();
    }

    public String summarize(String transcript) {
        if (appProperties.getGemini().getApiKey() == null || appProperties.getGemini().getApiKey().isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GEMINI_NOT_CONFIGURED",
                    "A chave da API Gemini não foi configurada.");
        }

        String truncatedTranscript = truncateTranscript(transcript);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + appProperties.getGemini().getModel()
                + ":generateContent?key="
                + appProperties.getGemini().getApiKey();

        try {
            String payload = objectMapper.writeValueAsString(new GeminiRequest(
                    new SystemInstruction(new Part[]{new Part(appProperties.getGemini().getSystemPrompt())}),
                    new Content[]{new Content(new Part[]{new Part("Transcrição do vídeo:\n" + truncatedTranscript)})}));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(appProperties.getGemini().getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw mapGeminiError(response.body(), response.statusCode());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode textNode = rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GEMINI_EMPTY_RESPONSE",
                        "A IA não retornou um resumo válido.");
            }

            return textNode.asText().trim();
        } catch (ApiException exception) {
            throw exception;
        } catch (java.net.http.HttpTimeoutException exception) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "GEMINI_TIMEOUT",
                    "Tempo limite excedido. Tente novamente.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "REQUEST_INTERRUPTED",
                    "A solicitação foi interrompida.");
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "GEMINI_CONNECTION_ERROR",
                    "Erro ao conectar com a inteligência artificial.");
        }
    }

    private String truncateTranscript(String transcript) {
        int limit = appProperties.getGemini().getMaxTranscriptCharacters();
        if (transcript.length() <= limit) {
            return transcript;
        }

        return transcript.substring(0, limit)
                + "\n\n[Transcrição truncada automaticamente para respeitar o limite seguro da API.]";
    }

    private ApiException mapGeminiError(String responseBody, int statusCode) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String message = rootNode.path("error").path("message").asText();
            if (message == null || message.isBlank()) {
                message = "Erro ao conectar com a inteligência artificial.";
            }

            HttpStatus status = statusCode == 504 ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY;
            String code = status == HttpStatus.GATEWAY_TIMEOUT ? "GEMINI_TIMEOUT" : "GEMINI_API_ERROR";
            return new ApiException(status, code, status == HttpStatus.GATEWAY_TIMEOUT
                    ? "Tempo limite excedido. Tente novamente."
                    : message);
        } catch (IOException exception) {
            return new ApiException(HttpStatus.BAD_GATEWAY, "GEMINI_API_ERROR",
                    "Erro ao conectar com a inteligência artificial.");
        }
    }

    private static class GeminiRequest {
        private final SystemInstruction system_instruction;
        private final Content[] contents;

        private GeminiRequest(SystemInstruction systemInstruction, Content[] contents) {
            this.system_instruction = systemInstruction;
            this.contents = contents;
        }

        public SystemInstruction getSystem_instruction() {
            return system_instruction;
        }

        public Content[] getContents() {
            return contents;
        }
    }

    private static class SystemInstruction {
        private final Part[] parts;

        private SystemInstruction(Part[] parts) {
            this.parts = parts;
        }

        public Part[] getParts() {
            return parts;
        }
    }

    private static class Content {
        private final Part[] parts;

        private Content(Part[] parts) {
            this.parts = parts;
        }

        public Part[] getParts() {
            return parts;
        }
    }

    private static class Part {
        private final String text;

        private Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
