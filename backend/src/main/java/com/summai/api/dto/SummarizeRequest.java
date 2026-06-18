package com.summai.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class SummarizeRequest {

    @NotBlank(message = "Informe a URL do vídeo.")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=[\\w-]{11}.*|youtu\\.be/[\\w-]{11}.*|youtube\\.com/shorts/[\\w-]{11}.*)$",
            message = "Informe uma URL válida do YouTube.")
    private String videoUrl;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
