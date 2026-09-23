package com.dando.pacepilot.assistant.api;

import com.dando.pacepilot.assistant.TrainingAnswerSource;

public record TrainingAnswerSourceResponse(
        int citationNumber,
        String chunkId,
        String documentTitle,
        String sectionTitle,
        String source,
        String content,
        double similarity
) {

    public static TrainingAnswerSourceResponse from(TrainingAnswerSource source) {
        return new TrainingAnswerSourceResponse(
                source.citationNumber(),
                source.chunkId(),
                source.documentTitle(),
                source.sectionTitle(),
                source.source(),
                source.content(),
                source.similarity()
        );
    }
}