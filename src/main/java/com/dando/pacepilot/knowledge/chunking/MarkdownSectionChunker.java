package com.dando.pacepilot.knowledge.chunking;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MarkdownSectionChunker {

    private static final String DOCUMENT_TITLE_PREFIX = "# ";
    private static final String SECTION_TITLE_PREFIX = "## ";

    public List<TrainingChunk> createChunks(TrainingDocument document) {
        Objects.requireNonNull(document, "Training document cannot be null");

        List<TrainingChunk> chunks = new ArrayList<>();

        // Every document begins with an artificial introduction
        String currentSectionTitle = "Introduction";
        StringBuilder currentContent = new StringBuilder();
        int chunkNumber = 1;

        String[] lines = document.content().split("\\R", -1);

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Detect new section
            if (trimmedLine.startsWith(SECTION_TITLE_PREFIX)) {
                // Add chunk from the previous document section and update chunk number
                chunkNumber = addChunkIfNotBlank(
                        chunks,
                        document,
                        currentSectionTitle,
                        currentContent,
                        chunkNumber
                );

                // reset chunk for new document section
                currentContent.setLength(0);
                currentSectionTitle = trimmedLine.substring(SECTION_TITLE_PREFIX.length()).trim();
                continue;
            }

            if (trimmedLine.startsWith(DOCUMENT_TITLE_PREFIX)) {
                continue;
            }

            // Add line to current chunk
            currentContent.append(line).append('\n');
        }

        addChunkIfNotBlank(
                chunks,
                document,
                currentSectionTitle,
                currentContent,
                chunkNumber
        );

        return List.copyOf(chunks);
    }

    // Helper to add TrainingChunk to chunks list and return updated chunk number
    private int addChunkIfNotBlank(
            List<TrainingChunk> chunks,
            TrainingDocument document,
            String sectionTitle,
            StringBuilder sectionContent,
            int chunkNumber
    ) {
        String content = sectionContent.toString().strip();

        if (content.isBlank()) {
            return chunkNumber;
        }

        String chunkId = document.id() + "-chunk-" + chunkNumber;

        TrainingChunk chunk = new TrainingChunk(
                chunkId,
                document.id(),
                document.title(),
                document.source(),
                sectionTitle,
                chunkNumber,
                content
        );

        chunks.add(chunk);

        return chunkNumber + 1;
    }
}