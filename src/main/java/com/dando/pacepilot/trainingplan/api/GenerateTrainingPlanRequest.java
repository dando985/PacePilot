package com.dando.pacepilot.trainingplan.api;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Represents the API request body for generating a training plan based on:
// - start and end dates
// - fitness goal
// - available training days per week
// - distance unit
public record GenerateTrainingPlanRequest(

        @NotBlank
        String name,

        @NotNull
        @FutureOrPresent(message = "Start date cannot be in the past")
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
        DistanceUnit distanceUnit
) {

    @AssertTrue(message = "End date cannot be before start date.")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Goal target date cannot be before the plan's start date.")
    public boolean isGoalDateValid() {
        if (goal == null || goal.targetDate() == null || startDate == null) {
            return true;
        }

        return !goal.targetDate().isBefore(startDate);
    }
}