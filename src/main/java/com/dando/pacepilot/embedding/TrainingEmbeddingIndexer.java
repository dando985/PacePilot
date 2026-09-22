package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrainingEmbeddingIndexer {

    private final EmbeddingProvider embeddingProvider;

    public TrainingEmbeddingIndexer(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    public List<EmbeddedTrainingChunk> createIndex(List<TrainingChunk> chunks) {
        List<EmbeddedTrainingChunk> index = new ArrayList<>();

        for (TrainingChunk chunk : chunks) {
            String embeddingText = createEmbeddingText(chunk);

            double[] embedding = embeddingProvider.createEmbedding(embeddingText);

            EmbeddedTrainingChunk embeddedChunk = new EmbeddedTrainingChunk(chunk, embedding);

            index.add(embeddedChunk);
        }

        return List.copyOf(index);
    }

    private String createEmbeddingText(TrainingChunk chunk) {
        return """
                Document: %s
                Section: %s
                Training guidance:
                %s
                """.formatted(
                chunk.documentTitle(),
                chunk.sectionTitle(),
                chunk.content()
        );
    }
}