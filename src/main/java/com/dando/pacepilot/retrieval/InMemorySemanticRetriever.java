package com.dando.pacepilot.retrieval;

import com.dando.pacepilot.embedding.EmbeddedTrainingChunk;
import com.dando.pacepilot.embedding.EmbeddingProvider;
import com.dando.pacepilot.embedding.SemanticIndexService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class InMemorySemanticRetriever implements SemanticRetriever {

    private final EmbeddingProvider embeddingProvider;
    private final SemanticIndexService semanticIndexService;
    private final CosineSimilarityCalculator similarityCalculator;

    public InMemorySemanticRetriever(
            EmbeddingProvider embeddingProvider,
            SemanticIndexService semanticIndexService,
            CosineSimilarityCalculator similarityCalculator
    ) {
        this.embeddingProvider = embeddingProvider;
        this.semanticIndexService = semanticIndexService;
        this.similarityCalculator = similarityCalculator;
    }

    @Override
    public List<SemanticSearchResult> search(String query, int limit) {
        validateSearch(query, limit);

        // Calculate vector embedding for query
        double[] queryEmbedding = embeddingProvider.createEmbedding(query);

        // Calculate vector embeddings from documents
        List<EmbeddedTrainingChunk> index = semanticIndexService.getOrCreateIndex();

        // calculate cosine similarity score for each document chunk and return list sorted from high to low with specified search limit
        return index.stream()
                .map(embeddedChunk -> createSearchResult(embeddedChunk, queryEmbedding))
                .sorted(Comparator.comparingDouble(SemanticSearchResult::similarity).reversed())
                .limit(limit)
                .toList();
    }

    // helper to calculate cosine similarity between query and embedded chunks
    private SemanticSearchResult createSearchResult(EmbeddedTrainingChunk embeddedChunk, double[] queryEmbedding) {
        double similarity = similarityCalculator.calculate(queryEmbedding, embeddedChunk.getEmbedding());

        return new SemanticSearchResult(embeddedChunk.getChunk(), similarity);
    }

    // helper to verify input query and search limit are valid
    private void validateSearch(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank.");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("Search result limit must be greater than zero.");
        }
    }
}