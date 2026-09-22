package com.dando.pacepilot.knowledge.api;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;

public record TrainingChunkResponse(
        String id,
        String documentId,
        String documentTitle,
        String source,
        String sectionTitle,
        int chunkNumber,
        String content
) {

    public static TrainingChunkResponse from(TrainingChunk chunk) {
        return new TrainingChunkResponse(
                chunk.id(),
                chunk.documentId(),
                chunk.documentTitle(),
                chunk.source(),
                chunk.sectionTitle(),
                chunk.chunkNumber(),
                chunk.content()
        );
    }
}