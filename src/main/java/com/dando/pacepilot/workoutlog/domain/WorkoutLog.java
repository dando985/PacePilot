package com.dando.pacepilot.workoutlog.domain;

import com.dando.pacepilot.shared.domain.DistanceUnit;

import java.time.LocalDate;
import java.util.UUID;

// Records completion of a planned workout by an athlete
public record WorkoutLog(
        UUID id,
        UUID athleteProfileId,
        UUID plannedWorkoutId,
        LocalDate completedDate,
        Double actualDistance,
        DistanceUnit distanceUnit,
        Integer actualDurationMinutes,
        String notes
) {

    public WorkoutLog {
        if (id == null) {
            throw new IllegalArgumentException("Workout log ID is required.");
        }

        if (athleteProfileId == null) {
            throw new IllegalArgumentException("Athlete profile ID is required.");
        }

        if (plannedWorkoutId == null) {
            throw new IllegalArgumentException("Planned workout ID is required.");
        }

        if (completedDate == null) {
            throw new IllegalArgumentException("Completion date is required.");
        }

        if (actualDistance != null
                && (!Double.isFinite(actualDistance) || actualDistance <= 0)) {
            throw new IllegalArgumentException("Actual distance must be a finite positive number.");
        }

        if (actualDurationMinutes != null && actualDurationMinutes <= 0) {
            throw new IllegalArgumentException("Actual duration must be positive.");
        }

        boolean hasDistance = actualDistance != null;
        boolean hasDistanceUnit = distanceUnit != null;

        if (hasDistance != hasDistanceUnit) {
            throw new IllegalArgumentException("Actual distance and distance unit must be provided together.");
        }

        notes = notes == null || notes.isBlank() ? null : notes.strip();
    }
}