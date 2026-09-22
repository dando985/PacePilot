package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.service.TrainingKnowledgeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemanticIndexService {

    private final TrainingKnowledgeService knowledgeService;
    private final TrainingEmbeddingIndexer embeddingIndexer;

    private List<EmbeddedTrainingChunk> index;

    public SemanticIndexService(TrainingKnowledgeService knowledgeService, TrainingEmbeddingIndexer embeddingIndexer) {
        this.knowledgeService = knowledgeService;
        this.embeddingIndexer = embeddingIndexer;
    }

    public synchronized List<EmbeddedTrainingChunk>
    getOrCreateIndex() {
        if (index == null) {
            index = buildIndex();
        }

        return index;
    }

    public synchronized List<EmbeddedTrainingChunk>
    rebuildIndex() {
        index = buildIndex();
        return index;
    }

    private List<EmbeddedTrainingChunk> buildIndex() {
        List<TrainingChunk> chunks = knowledgeService.getAllChunks();

        List<EmbeddedTrainingChunk> createdIndex = embeddingIndexer.createIndex(chunks);

        return List.copyOf(createdIndex);
    }
}