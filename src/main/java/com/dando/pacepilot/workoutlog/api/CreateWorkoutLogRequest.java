package com.dando.pacepilot.workoutlog.api;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateWorkoutLogRequest(
        @NotNull @PastOrPresent LocalDate completedDate,
        @Positive Double actualDistance,
        DistanceUnit distanceUnit,
        @Positive Integer actualDurationMinutes,
        String notes
) {
}