package com.dando.pacepilot.trainingplan.api;

import com.dando.pacepilot.trainingplan.domain.GoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateFitnessGoalRequest(

        @NotNull(message = "Goal type is required")
        GoalType type,

        @NotNull(message = "Target date is required")
        LocalDate targetDate,

        @Positive(message = "Target time must be greater than zero")
        Integer targetTimeMinutes
) {
}