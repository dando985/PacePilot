package com.dando.pacepilot.knowledge.service;

import com.dando.pacepilot.knowledge.chunking.TrainingDocumentChunker;
import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import com.dando.pacepilot.knowledge.loader.TrainingDocumentLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingKnowledgeServiceTest {

    @Mock
    private TrainingDocumentLoader documentLoader;

    @Mock
    private TrainingDocumentChunker documentChunker;

    private TrainingKnowledgeService knowledgeService;
    private TrainingChunk runningChunk;
    private TrainingChunk fitnessChunk;

    @BeforeEach
    void setUp() {
        TrainingDocument runningDocument =
                new TrainingDocument(
                        "running",
                        "Running Guide",
                        "knowledge/running.md",
                        "Running content"
                );

        TrainingDocument fitnessDocument =
                new TrainingDocument(
                        "fitness",
                        "Fitness Guide",
                        "knowledge/fitness.md",
                        "Fitness content"
                );

        runningChunk = new TrainingChunk(
                "running-chunk-1",
                "running",
                "Running Guide",
                "knowledge/running.md",
                "Getting Started",
                1,
                "Start with manageable running sessions."
        );

        fitnessChunk = new TrainingChunk(
                "fitness-chunk-1",
                "fitness",
                "Fitness Guide",
                "knowledge/fitness.md",
                "Weekly Activity",
                1,
                "Build a sustainable weekly routine."
        );

        when(documentLoader.loadDocuments())
                .thenReturn(List.of(
                        runningDocument,
                        fitnessDocument
                ));

        when(documentChunker.createChunks(runningDocument)).thenReturn(List.of(runningChunk));
        when(documentChunker.createChunks(fitnessDocument)).thenReturn(List.of(fitnessChunk));

        knowledgeService = new TrainingKnowledgeService(documentLoader, documentChunker);
    }

    @Test
    void loadsAndCombinesChunksFromEveryDocument() {
        assertThat(knowledgeService.getAllChunks()).containsExactly(runningChunk, fitnessChunk);

        verify(documentLoader).loadDocuments();

        verify(documentChunker)
                .createChunks(
                        new TrainingDocument(
                                "running",
                                "Running Guide",
                                "knowledge/running.md",
                                "Running content"
                        )
                );

        verify(documentChunker)
                .createChunks(
                        new TrainingDocument(
                                "fitness",
                                "Fitness Guide",
                                "knowledge/fitness.md",
                                "Fitness content"
                        )
                );
    }
}