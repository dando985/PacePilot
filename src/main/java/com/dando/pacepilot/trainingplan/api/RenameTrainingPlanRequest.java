package com.dando.pacepilot.trainingplan.api;

import jakarta.validation.constraints.NotBlank;

public record RenameTrainingPlanRequest(

        @NotBlank(message = "Training plan name is required")
        String name
) {
}