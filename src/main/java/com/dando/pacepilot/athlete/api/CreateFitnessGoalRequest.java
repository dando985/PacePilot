package com.dando.pacepilot.athlete.api;

import com.dando.pacepilot.athlete.domain.GoalType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateFitnessGoalRequest(

        @NotNull(message = "Goal type is required")
        GoalType type,

        @NotNull(message = "Target date is required")
        @FutureOrPresent(message = "Target date cannot be in the past")
        LocalDate targetDate,

        @Positive(message = "Target time must be greater than zero")
        Integer targetTimeMinutes
) {
}