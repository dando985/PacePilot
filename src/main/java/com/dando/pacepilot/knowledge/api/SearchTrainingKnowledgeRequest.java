package com.dando.pacepilot.knowledge.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchTrainingKnowledgeRequest(

        @NotBlank(message = "Query is required.")
        @Size(max = 500, message = "Query must not exceed 500 characters.")
        String query,

        @Min(value = 1, message = "Limit must be at least 1.")
        @Max(value = 10, message = "Limit must not exceed 10.")
        Integer limit
) {

    private static final int DEFAULT_LIMIT = 3;

    public int resolvedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}