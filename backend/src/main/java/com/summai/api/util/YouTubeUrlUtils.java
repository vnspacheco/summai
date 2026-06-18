package com.summai.api.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public final class YouTubeUrlUtils {

    private static final List<String> YOUTUBE_HOSTS = List.of(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "youtu.be",
            "www.youtu.be");

    private YouTubeUrlUtils() {
    }

    public static String extractVideoId(String rawUrl) {
        try {
            String normalizedUrl = rawUrl.startsWith("http") ? rawUrl : "https://" + rawUrl;
            URI uri = new URI(normalizedUrl);
            String host = uri.getHost();

            if (host == null || YOUTUBE_HOSTS.stream().noneMatch(host::equalsIgnoreCase)) {
                throw new IllegalArgumentException("Informe uma URL válida do YouTube.");
            }

            if (host.toLowerCase().contains("youtu.be")) {
                String path = uri.getPath();
                if (path == null || path.length() < 2) {
                    throw new IllegalArgumentException("Não foi possível identificar o vídeo informado.");
                }
                return sanitizeVideoId(path.substring(1));
            }

            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.startsWith("/watch")) {
                return sanitizeVideoId(extractQueryValue(uri.getQuery(), "v"));
            }

            if (path.startsWith("/shorts/")) {
                return sanitizeVideoId(path.substring("/shorts/".length()));
            }

            throw new IllegalArgumentException("Informe uma URL válida do YouTube.");
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Informe uma URL válida do YouTube.");
        }
    }

    private static String extractQueryValue(String query, String key) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Não foi possível identificar o vídeo informado.");
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && key.equals(parts[0])) {
                return parts[1];
            }
        }
        throw new IllegalArgumentException("Não foi possível identificar o vídeo informado.");
    }

    private static String sanitizeVideoId(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("Não foi possível identificar o vídeo informado.");
        }

        String cleaned = videoId.length() > 11 ? videoId.substring(0, 11) : videoId;
        if (!cleaned.matches("[\\w-]{11}")) {
            throw new IllegalArgumentException("Não foi possível identificar o vídeo informado.");
        }

        return cleaned;
    }
}
