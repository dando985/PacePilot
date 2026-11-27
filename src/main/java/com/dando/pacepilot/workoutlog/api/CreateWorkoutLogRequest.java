package com.dando.pacepilot.workoutlog.api;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

// Request object for creating a new workout log
// Associated athlete ID is derived from the training plan ID and workout ID, so it is not included in this request.
public record CreateWorkoutLogRequest(
        @NotNull(message = "Completion date is required")
        @PastOrPresent(message = "Completion date cannot be in the future")
        LocalDate completedDate,

        @Positive(message = "Actual distance must be greater than zero")
        Double actualDistance,

        DistanceUnit distanceUnit,

        @Positive(message = "Actual duration must be greater than zero")
        Integer actualDurationMinutes,

        @PositiveOrZero(message = "Step count cannot be negative")
        Integer stepCount,

        @PositiveOrZero(message = "Calories burned cannot be negative")
        Integer caloriesBurnedKcal,

        @Positive(message = "Average heart rate must be greater than zero")
        Integer averageHeartRateBpm,

        String notes
) {
}