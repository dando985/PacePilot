package com.dando.pacepilot.trainingplan.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateTrainingWeekRequest(
        @Min(1) int weekNumber,
        @NotNull LocalDate startDate,
        @NotEmpty List<@NotNull @Valid CreatePlannedWorkoutRequest> workouts
) {
}