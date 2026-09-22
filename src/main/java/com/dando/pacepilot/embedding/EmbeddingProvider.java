package com.dando.pacepilot.embedding;

public interface EmbeddingProvider {

    double[] createEmbedding(String text);
}