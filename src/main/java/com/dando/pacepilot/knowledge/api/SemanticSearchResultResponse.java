package com.dando.pacepilot.knowledge.api;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.retrieval.SemanticSearchResult;

public record SemanticSearchResultResponse(
        String chunkId,
        String documentTitle,
        String sectionTitle,
        String source,
        int chunkNumber,
        String content,
        double similarity
) {

    public static SemanticSearchResultResponse from(SemanticSearchResult result) {
        TrainingChunk chunk = result.chunk();

        return new SemanticSearchResultResponse(
                chunk.id(),
                chunk.documentTitle(),
                chunk.sectionTitle(),
                chunk.source(),
                chunk.chunkNumber(),
                chunk.content(),
                result.similarity()
        );
    }
}