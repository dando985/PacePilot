package com.dando.pacepilot.retrieval;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;

import java.util.Objects;

public record SemanticSearchResult(TrainingChunk chunk, double similarity) {

    public SemanticSearchResult {
        Objects.requireNonNull(chunk, "Training chunk must not be null.");

        if (!Double.isFinite(similarity)) {
            throw new IllegalArgumentException("Similarity must be a finite number.");
        }
    }
}