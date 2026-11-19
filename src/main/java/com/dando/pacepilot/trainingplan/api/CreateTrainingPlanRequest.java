package com.dando.pacepilot.trainingplan.api;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateTrainingPlanRequest(

        @NotBlank
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull(message = "Fitness goal is required")
        @Valid
        CreateFitnessGoalRequest goal,

        @NotNull(message = "Available training days are required")
        @Min(value = 1, message = "At least one training day is required")
        @Max(value = 7, message = "Training days cannot exceed seven")
        Integer availableTrainingDaysPerWeek,

        @NotNull(message = "Distance unit is required")
        DistanceUnit distanceUnit,

        @NotEmpty
        List<@NotNull @Valid CreateTrainingWeekRequest> weeks
) {
}