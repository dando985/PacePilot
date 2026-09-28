package com.dando.pacepilot.assistant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AskTrainingAssistantRequest(

        @NotNull(message = "Athlete profile ID is required.")
        UUID athleteProfileId,

        @NotBlank(message = "Question is required.")
        @Size(max = 500, message = "Question must not exceed 500 characters.")
        String question
) {
}