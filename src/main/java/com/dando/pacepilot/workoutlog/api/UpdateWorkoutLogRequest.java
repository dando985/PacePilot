package com.dando.pacepilot.workoutlog.api;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record UpdateWorkoutLogRequest(

        @NotNull(message = "Completion date is required")
        @PastOrPresent(message = "Completion date cannot be in the future")
        LocalDate completedDate,

        @Positive(message = "Actual distance must be greater than zero")
        Double actualDistance,

        DistanceUnit distanceUnit,

        @Positive(message = "Actual duration must be greater than zero")
        Integer actualDurationMinutes,

        String notes
) {
}