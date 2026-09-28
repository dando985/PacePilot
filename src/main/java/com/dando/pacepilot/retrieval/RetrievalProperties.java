package com.dando.pacepilot.retrieval;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pacepilot.retrieval")
public record RetrievalProperties(

        @NotNull
        @DecimalMin(
                value = "0.0",
                message = "Minimum similarity cannot be below 0"
        )
        @DecimalMax(
                value = "1.0",
                message = "Minimum similarity cannot exceed 1"
        )
        Double minimumSimilarity
) {
}