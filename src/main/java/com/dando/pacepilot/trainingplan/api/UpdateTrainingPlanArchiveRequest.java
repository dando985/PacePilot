package com.dando.pacepilot.trainingplan.api;

import jakarta.validation.constraints.NotNull;

public record UpdateTrainingPlanArchiveRequest(

        @NotNull(message = "Archive status is required")
        Boolean archived
) {
}