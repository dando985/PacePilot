package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.service.TrainingKnowledgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticIndexServiceTest {

    @Mock
    private TrainingKnowledgeService knowledgeService;

    @Mock
    private EmbeddingProvider embeddingProvider;

    @InjectMocks
    private SemanticIndexService semanticIndexService;

    @Test
    void buildsIndexOnceAndReusesIt() {
        TrainingChunk chunk = createTrainingChunk();

        when(knowledgeService.getAllChunks()).thenReturn(List.of(chunk));

        when(embeddingProvider.createEmbedding(anyString()))
                .thenReturn(new double[]{0.10, 0.20, 0.30});

        List<EmbeddedTrainingChunk> firstResult = semanticIndexService.getOrCreateIndex();

        List<EmbeddedTrainingChunk> secondResult = semanticIndexService.getOrCreateIndex();

        assertThat(firstResult).hasSize(1);

        assertThat(firstResult.getFirst().getChunk()).isSameAs(chunk);

        assertThat(firstResult.getFirst().getEmbedding()).containsExactly(0.10, 0.20, 0.30);

        assertThat(secondResult).isSameAs(firstResult);

        verify(knowledgeService).getAllChunks();

        verify(embeddingProvider).createEmbedding(anyString());
    }

    @Test
    void createsAnEmbeddingForEveryChunkAndPreservesOrder() {
        TrainingChunk firstChunk = createTrainingChunk();

        TrainingChunk secondChunk =
                new TrainingChunk(
                        "fitness-chunk-1",
                        "fitness-document",
                        "General Fitness Foundations",
                        "knowledge/general-fitness-foundations.md",
                        "Strength training",
                        1,
                        "Adults should perform regular strength training."
                );

        when(knowledgeService.getAllChunks())
                .thenReturn(List.of(firstChunk, secondChunk));

        when(embeddingProvider.createEmbedding(anyString()))
                .thenReturn(
                        new double[]{0.10, 0.20, 0.30},
                        new double[]{0.40, 0.50, 0.60}
                );

        List<EmbeddedTrainingChunk> result = semanticIndexService.getOrCreateIndex();

        assertThat(result)
                .extracting(EmbeddedTrainingChunk::getChunk)
                .containsExactly(firstChunk, secondChunk);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(embeddingProvider, times(2)).createEmbedding(textCaptor.capture());

        List<String> embeddedTexts = textCaptor.getAllValues();

        assertThat(embeddedTexts.get(0))
                .contains(
                        "Beginner Running Foundations",
                        "Recovery between runs",
                        "Beginners should recover"
                );

        assertThat(embeddedTexts.get(1))
                .contains(
                        "General Fitness Foundations",
                        "Strength training",
                        "Adults should perform"
                );
    }

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