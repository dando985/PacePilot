package com.dando.pacepilot.generation;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

// Sets configuration output token budget for structured generation requests, which is separate from the text generation budget
@Validated
@ConfigurationProperties(prefix = "pacepilot.generation.structured")
public record StructuredGenerationProperties(
        @DefaultValue("8192")
        @Min(1)
        int maxOutputTokens
) {
}