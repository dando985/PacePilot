package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.service.TrainingKnowledgeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SemanticIndexService {

    private final TrainingKnowledgeService knowledgeService;
    private final EmbeddingProvider embeddingProvider;

    private List<EmbeddedTrainingChunk> index;

    public SemanticIndexService(TrainingKnowledgeService knowledgeService, EmbeddingProvider embeddingProvider) {
        this.knowledgeService = knowledgeService;
        this.embeddingProvider = embeddingProvider;
    }

    public synchronized List<EmbeddedTrainingChunk> getOrCreateIndex() {
        if (index == null) {
            index = buildIndex();
        }

        return index;
    }

    private List<EmbeddedTrainingChunk> buildIndex() {
        List<TrainingChunk> chunks = knowledgeService.getAllChunks();

        List<EmbeddedTrainingChunk> createdIndex = new ArrayList<>();

        for (TrainingChunk chunk : chunks) {
            String embeddingText = createEmbeddingText(chunk);

            double[] embedding = embeddingProvider.createEmbedding(embeddingText);

            EmbeddedTrainingChunk embeddedChunk = new EmbeddedTrainingChunk(chunk, embedding);

            createdIndex.add(embeddedChunk);
        }

        return List.copyOf(createdIndex);
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