package com.dando.pacepilot.ollama;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "pacepilot.ollama")
public record OllamaProperties(
        @NotNull URI baseUrl,
        @NotBlank String embeddingModel,
        @NotBlank String chatModel
) {
}