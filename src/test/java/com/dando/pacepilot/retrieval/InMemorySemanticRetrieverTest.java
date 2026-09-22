package com.dando.pacepilot.retrieval;

import com.dando.pacepilot.embedding.EmbeddedTrainingChunk;
import com.dando.pacepilot.embedding.EmbeddingProvider;
import com.dando.pacepilot.embedding.SemanticIndexService;
import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemorySemanticRetrieverTest {

    @Mock
    private EmbeddingProvider embeddingProvider;

    @Mock
    private SemanticIndexService semanticIndexService;

    private InMemorySemanticRetriever retriever;

    @BeforeEach
    void setUp() {
        retriever = new InMemorySemanticRetriever(
                embeddingProvider,
                semanticIndexService,
                new CosineSimilarityCalculator()
        );
    }

    @Test
    void returnsMostSimilarChunksFirst() {
        String query = "How much recovery does a beginner need?";

        double[] queryEmbedding = {1.0, 0.0};

        TrainingChunk recoveryChunk = createChunk(
                "running-chunk-1",
                "Recovery between runs",
                "Beginners should recover between running sessions.",
                1
        );

        TrainingChunk easyRunningChunk = createChunk(
                "running-chunk-2",
                "Easy running",
                "Easy running helps develop aerobic fitness.",
                2
        );

        TrainingChunk strengthChunk = createChunk(
                "fitness-chunk-1",
                "Strength training",
                "Adults should perform regular strength training.",
                3
        );

        List<EmbeddedTrainingChunk> index = List.of(
                new EmbeddedTrainingChunk(strengthChunk, new double[]{0.0, 1.0}),
                new EmbeddedTrainingChunk(easyRunningChunk, new double[]{0.8, 0.6}),
                new EmbeddedTrainingChunk(recoveryChunk, new double[]{1.0, 0.0})
        );

        when(embeddingProvider.createEmbedding(query)).thenReturn(queryEmbedding);

        when(semanticIndexService.getOrCreateIndex()).thenReturn(index);

        List<SemanticSearchResult> results = retriever.search(query, 2);

        assertThat(results).hasSize(2);

        // Check that top result is recoveryChunk which has the same vector embedding as query
        assertThat(results.get(0).chunk()).isSameAs(recoveryChunk);
        assertEquals(1.0, results.get(0).similarity(), 0.000001);

        assertThat(results.get(1).chunk()).isSameAs(easyRunningChunk);
        assertEquals(0.8, results.get(1).similarity(), 0.000001);

        verify(embeddingProvider).createEmbedding(query);

        verify(semanticIndexService).getOrCreateIndex();
    }

    @Test
    void rejectsBlankQuery() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> retriever.search(" ", 3))
                .withMessage("Search query must not be blank.");
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> retriever.search("beginner running", 0))
                .withMessage("Search result limit must be greater than zero.");
    }

    // helper to create sample chunk
    private TrainingChunk createChunk(
            String id,
            String sectionTitle,
            String content,
            int chunkNumber
    ) {
        return new TrainingChunk(
                id,
                "training-document",
                "Training Foundations",
                "knowledge/training-foundations.md",
                sectionTitle,
                chunkNumber,
                content
        );
    }
}