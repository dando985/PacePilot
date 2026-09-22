package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;

import java.util.Objects;

public final class EmbeddedTrainingChunk {

    private final TrainingChunk chunk;
    private final double[] embedding;

    public EmbeddedTrainingChunk(TrainingChunk chunk, double[] embedding) {
        this.chunk = Objects.requireNonNull(chunk, "Training chunk must not be null.");

        Objects.requireNonNull(embedding, "Embedding must not be null.");

        if (embedding.length == 0) {
            throw new IllegalArgumentException("Embedding must not be empty.");
        }

        this.embedding = embedding.clone();
    }

    public TrainingChunk getChunk() {
        return chunk;
    }

    public double[] getEmbedding() {
        return embedding.clone();
    }
}