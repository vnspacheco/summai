package com.summai.api.service;

import com.summai.api.exception.ApiException;
import io.github.thoroldvix.api.Transcript;
import io.github.thoroldvix.api.TranscriptApiFactory;
import io.github.thoroldvix.api.TranscriptContent;
import io.github.thoroldvix.api.TranscriptList;
import io.github.thoroldvix.api.TranscriptRetrievalException;
import io.github.thoroldvix.api.YoutubeTranscriptApi;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class YouTubeTranscriptService {

    private static final String[] PREFERRED_PORTUGUESE_LANGUAGES = {"pt", "pt-BR", "pt-PT"};
    private final YoutubeTranscriptApi youtubeTranscriptApi;

    public YouTubeTranscriptService() {
        this.youtubeTranscriptApi = TranscriptApiFactory.createDefault();
    }

    public String fetchTranscript(String videoId) {
        try {
            TranscriptList transcriptList = youtubeTranscriptApi.listTranscripts(videoId);
            Transcript transcript = selectTranscript(transcriptList);
            TranscriptContent transcriptContent = transcript.fetch();
            String cleanedTranscript = flattenTranscript(transcriptContent);

            if (cleanedTranscript.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSCRIPT_NOT_FOUND",
                        "Não foi possível encontrar legendas para este vídeo.");
            }

            return cleanedTranscript;
        } catch (TranscriptRetrievalException exception) {
            throw mapTranscriptException(exception);
        }
    }

    private Transcript selectTranscript(TranscriptList transcriptList) throws TranscriptRetrievalException {
        try {
            return transcriptList.findTranscript(PREFERRED_PORTUGUESE_LANGUAGES);
        } catch (TranscriptRetrievalException exception) {
            Iterator<Transcript> iterator = transcriptList.iterator();
            if (!iterator.hasNext()) {
                throw exception;
            }

            Transcript fallbackTranscript = iterator.next();
            if (!fallbackTranscript.getLanguageCode().startsWith("pt") && fallbackTranscript.isTranslatable()) {
                return fallbackTranscript.translate("pt");
            }

            return fallbackTranscript;
        }
    }

    String flattenTranscript(TranscriptContent transcriptContent) {
        if (transcriptContent == null || transcriptContent.getContent() == null) {
            return "";
        }

        return transcriptContent.getContent().stream()
                .map(TranscriptContent.Fragment::getText)
                .map(this::normalizeTranscriptText)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String normalizeTranscriptText(String text) {
        return text
                .replace('\u00A0', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private ApiException mapTranscriptException(TranscriptRetrievalException exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage();
        String normalizedMessage = message.toLowerCase();

        if (normalizedMessage.contains("transcript") && normalizedMessage.contains("disabled")
                || normalizedMessage.contains("no transcripts were found")
                || normalizedMessage.contains("subtitles are disabled")) {
            return new ApiException(HttpStatus.BAD_REQUEST, "TRANSCRIPT_NOT_FOUND",
                    "Não foi possível encontrar legendas para este vídeo.");
        }

        if (normalizedMessage.contains("video unavailable")
                || normalizedMessage.contains("video is no longer available")
                || normalizedMessage.contains("private video")
                || normalizedMessage.contains("login")) {
            return new ApiException(HttpStatus.BAD_REQUEST, "VIDEO_INACCESSIBLE",
                    "O vídeo informado está inacessível.");
        }

        if (normalizedMessage.contains("429")
                || normalizedMessage.contains("too many requests")
                || normalizedMessage.contains("ip")) {
            return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "YOUTUBE_RATE_LIMIT",
                    "O YouTube bloqueou temporariamente a consulta de legendas. Tente novamente mais tarde.");
        }

        return new ApiException(HttpStatus.BAD_GATEWAY, "YOUTUBE_TRANSCRIPT_ERROR",
                "Não foi possível obter a transcrição do YouTube.");
    }
}
