package com.dando.pacepilot.trainingplan.api;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreatePlannedWorkoutRequest(
        @NotNull LocalDate scheduledDate,
        @NotNull WorkoutType type,
        @NotBlank String title,
        @Positive Double targetDistance,
        DistanceUnit distanceUnit,
        @Positive Integer targetDurationMinutes,
        @NotBlank String instructions
) {
}