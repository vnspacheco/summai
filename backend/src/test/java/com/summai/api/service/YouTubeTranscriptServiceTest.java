package com.summai.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.thoroldvix.api.TranscriptContent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class YouTubeTranscriptServiceTest {

    private final YouTubeTranscriptService service = new YouTubeTranscriptService();

    @Test
    void deveTransformarFragmentosEmTextoCorrente() {
        TranscriptContent transcriptContent = new TranscriptContent(Arrays.asList(
                new TranscriptContent.Fragment("Olá", 0.0, 1.0),
                new TranscriptContent.Fragment("  mundo\n", 1.0, 1.0),
                new TranscriptContent.Fragment(" ", 2.0, 1.0),
                new TranscriptContent.Fragment("teste", 3.0, 1.0)));

        assertEquals("Olá mundo teste", service.flattenTranscript(transcriptContent));
    }
}
