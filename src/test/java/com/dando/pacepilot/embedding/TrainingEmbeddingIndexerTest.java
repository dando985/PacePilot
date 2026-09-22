package com.dando.pacepilot.embedding;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingEmbeddingIndexerTest {

    @Mock
    private EmbeddingProvider embeddingProvider;

    @InjectMocks
    private TrainingEmbeddingIndexer indexer;

    @Test
    void createsEmbeddingForEveryTrainingChunk() {
        TrainingChunk firstChunk = new TrainingChunk(
                "running-chunk-1",
                "running-document",
                "Beginner Running Foundations",
                "knowledge/beginner-running-foundations.md",
                "Recovery between runs",
                1,
                "Beginners should recover between running sessions."
        );

        TrainingChunk secondChunk = new TrainingChunk(
                "fitness-chunk-1",
                "fitness-document",
                "General Fitness Foundations",
                "knowledge/general-fitness-foundations.md",
                "Strength training",
                1,
                "Adults should perform regular strength training."
        );

        double[] firstEmbedding = {0.10, 0.20, 0.30};

        double[] secondEmbedding = {0.40, 0.50, 0.60};

        when(embeddingProvider.createEmbedding(anyString())).thenReturn(firstEmbedding).thenReturn(secondEmbedding);

        List<EmbeddedTrainingChunk> result = indexer.createIndex(List.of(firstChunk, secondChunk));

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getChunk()).isSameAs(firstChunk);
        assertThat(result.get(0).getEmbedding()).containsExactly(firstEmbedding);

        assertThat(result.get(1).getChunk()).isSameAs(secondChunk);
        assertThat(result.get(1).getEmbedding()).containsExactly(secondEmbedding);

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
}