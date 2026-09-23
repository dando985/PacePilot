package com.dando.pacepilot.assistant.api;

import com.dando.pacepilot.assistant.TrainingAnswer;

import java.util.List;

public record TrainingAnswerResponse(String question, String answer, List<TrainingAnswerSourceResponse> sources) {

    public static TrainingAnswerResponse from(TrainingAnswer trainingAnswer) {
        List<TrainingAnswerSourceResponse> sources =
                trainingAnswer.sources()
                        .stream()
                        .map(TrainingAnswerSourceResponse::from)
                        .toList();

        return new TrainingAnswerResponse(
                trainingAnswer.question(),
                trainingAnswer.answer(),
                sources
        );
    }
}