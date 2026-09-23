package com.dando.pacepilot.assistant;

import java.util.List;
import java.util.Objects;

public record TrainingAnswer(String question, String answer, List<TrainingAnswerSource> sources) {

    public TrainingAnswer {
        Objects.requireNonNull(question, "Question must not be null.");

        Objects.requireNonNull(answer, "Answer must not be null.");

        sources = List.copyOf(Objects.requireNonNull(sources, "Sources must not be null."));
    }
}