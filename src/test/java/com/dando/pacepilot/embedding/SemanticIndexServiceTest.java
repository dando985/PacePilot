package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.service.TrainingKnowledgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticIndexServiceTest {

    @Mock
    private TrainingKnowledgeService knowledgeService;

    @Mock
    private TrainingEmbeddingIndexer embeddingIndexer;

    @InjectMocks
    private SemanticIndexService semanticIndexService;

    @Test
    void buildsIndexOnceAndReusesIt() {
        TrainingChunk chunk = createTrainingChunk();

        List<TrainingChunk> chunks = List.of(chunk);

        List<EmbeddedTrainingChunk> createdIndex =
                List.of(
                        new EmbeddedTrainingChunk(
                                chunk,
                                new double[]{0.10, 0.20, 0.30}
                        )
                );

        when(knowledgeService.getAllChunks()).thenReturn(chunks);

        when(embeddingIndexer.createIndex(chunks)).thenReturn(createdIndex);

        // Build first semantic index
        List<EmbeddedTrainingChunk> firstResult = semanticIndexService.getOrCreateIndex();

        // Reuse cached index instead of rebuilding it
        List<EmbeddedTrainingChunk> secondResult = semanticIndexService.getOrCreateIndex();

        assertThat(firstResult).containsExactlyElementsOf(createdIndex);

        assertThat(secondResult).isSameAs(firstResult);

        verify(knowledgeService, times(1)).getAllChunks();

        verify(embeddingIndexer, times(1)).createIndex(chunks);
    }

    @Test
    void rebuildsTheExistingIndex() {
        TrainingChunk chunk = createTrainingChunk();

        List<TrainingChunk> chunks = List.of(chunk);

        List<EmbeddedTrainingChunk> firstIndex =
                List.of(
                        new EmbeddedTrainingChunk(
                                chunk,
                                new double[]{0.10, 0.20, 0.30}
                        )
                );

        List<EmbeddedTrainingChunk> rebuiltIndex =
                List.of(
                        new EmbeddedTrainingChunk(
                                chunk,
                                new double[]{0.40, 0.50, 0.60}
                        )
                );

        when(knowledgeService.getAllChunks()).thenReturn(chunks);

        when(embeddingIndexer.createIndex(chunks)).thenReturn(firstIndex).thenReturn(rebuiltIndex);

        // Build first semantic index
        List<EmbeddedTrainingChunk> originalResult = semanticIndexService.getOrCreateIndex();

        // Rebuild semantic index
        List<EmbeddedTrainingChunk> rebuiltResult = semanticIndexService.rebuildIndex();

        // Reuse the last cached semantic index (rebuilt index)
        List<EmbeddedTrainingChunk> cachedResult = semanticIndexService.getOrCreateIndex();

        assertThat(originalResult.getFirst().getEmbedding()).containsExactly(0.10, 0.20, 0.30);

        assertThat(rebuiltResult.getFirst().getEmbedding()).containsExactly(0.40, 0.50, 0.60);

        assertThat(cachedResult).isSameAs(rebuiltResult);

        verify(knowledgeService, times(2)).getAllChunks();

        verify(embeddingIndexer, times(2)).createIndex(chunks);
    }

    // helper to create sample training chunk
    private TrainingChunk createTrainingChunk() {
        return new TrainingChunk(
                "running-chunk-1",
                "running-document",
                "Beginner Running Foundations",
                "knowledge/beginner-running-foundations.md",
                "Recovery between runs",
                1,
                "Beginners should recover between running sessions."
        );
    }
}