package com.dando.pacepilot.knowledge.domain;

public record TrainingChunk(
        String id,
        String documentId,
        String documentTitle,
        String source,
        String sectionTitle,
        int chunkNumber,
        String content
) {
}