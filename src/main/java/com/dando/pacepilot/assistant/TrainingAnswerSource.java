package com.dando.pacepilot.assistant;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.retrieval.SemanticSearchResult;

public record TrainingAnswerSource(
        int citationNumber,
        String chunkId,
        String documentTitle,
        String sectionTitle,
        String source,
        String content,
        double similarity
) {

    public static TrainingAnswerSource from(int citationNumber, SemanticSearchResult result) {
        TrainingChunk chunk = result.chunk();

        return new TrainingAnswerSource(
                citationNumber,
                chunk.id(),
                chunk.documentTitle(),
                chunk.sectionTitle(),
                chunk.source(),
                chunk.content(),
                result.similarity()
        );
    }
}