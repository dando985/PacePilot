package com.dando.pacepilot.assistant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskTrainingAssistantRequest(

        @NotBlank(message = "Question is required.")
        @Size(max = 500, message = "Question must not exceed 500 characters.")
        String question
) {
}