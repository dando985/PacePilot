package com.dando.pacepilot.knowledge.chunking;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import com.dando.pacepilot.knowledge.loader.ClasspathTrainingDocumentLoader;
import com.dando.pacepilot.knowledge.loader.TrainingDocumentLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownSectionChunkerTest {

    private final TrainingDocumentChunker chunker = new MarkdownSectionChunker();

    @Test
    void createsOneChunkForEachMarkdownSection() {
        TrainingDocument document = new TrainingDocument(
                "test-guide",
                "Test Guide",
                "knowledge/test-guide.md",
                """
                # Test Guide

                Introductory text.

                ## First Section

                First section text.

                ## Second Section

                Second section text.
                """
        );

        List<TrainingChunk> chunks = chunker.createChunks(document);

        assertThat(chunks).hasSize(3);

        assertThat(chunks)
                .extracting(TrainingChunk::sectionTitle)
                .containsExactly(
                        "Introduction",
                        "First Section",
                        "Second Section"
                );

        assertThat(chunks)
                .extracting(TrainingChunk::id)
                .containsExactly(
                        "test-guide-chunk-1",
                        "test-guide-chunk-2",
                        "test-guide-chunk-3"
                );

        assertThat(chunks.getFirst().content()).isEqualTo("Introductory text.");
    }

    @Test
    void chunksTheRealKnowledgeDocuments() {
        TrainingDocumentLoader loader = new ClasspathTrainingDocumentLoader();

        List<TrainingChunk> chunks = loader
                .loadDocuments()
                .stream()
                .flatMap(document ->
                        chunker
                                .createChunks(document)
                                .stream()
                )
                .toList();

        assertThat(chunks)
                .extracting(TrainingChunk::sectionTitle)
                .contains(
                        "Starting with Run-Walk Sessions",
                        "Frequency and Recovery",
                        "Building a Weekly Routine",
                        "Choosing Activities"
                );

        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.id()).isNotBlank();
            assertThat(chunk.documentId()).isNotBlank();
            assertThat(chunk.sectionTitle()).isNotBlank();
            assertThat(chunk.content()).isNotBlank();
        });
    }
}