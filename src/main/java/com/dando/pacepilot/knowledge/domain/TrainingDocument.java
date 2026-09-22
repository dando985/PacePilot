package com.dando.pacepilot.knowledge.domain;

public record TrainingDocument(
        String id,
        String title,
        String source,
        String content
) {
}