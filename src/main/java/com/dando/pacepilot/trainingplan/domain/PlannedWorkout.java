package com.dando.pacepilot.trainingplan.domain;

import com.dando.pacepilot.shared.domain.DistanceUnit;

import java.time.LocalDate;
import java.util.UUID;

public record PlannedWorkout(
        UUID id,
        LocalDate scheduledDate,
        WorkoutType type,
        String title,
        Double targetDistance,
        DistanceUnit distanceUnit,
        Integer targetDurationMinutes,
        String instructions
) {

    public PlannedWorkout {
        if (id == null) {
            throw new IllegalArgumentException("Workout ID is required.");
        }

        if (scheduledDate == null) {
            throw new IllegalArgumentException("Scheduled date is required.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Workout type is required.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Workout title is required.");
        }

        if (targetDistance != null && (!Double.isFinite(targetDistance) || targetDistance <= 0)) {
            throw new IllegalArgumentException("Target distance must be a finite positive number.");
        }

        if (targetDurationMinutes != null && targetDurationMinutes <= 0) {
            throw new IllegalArgumentException("Target duration must be positive.");
        }

        boolean hasDistance = targetDistance != null;
        boolean hasDistanceUnit = distanceUnit != null;

        if (hasDistance != hasDistanceUnit) {
            throw new IllegalArgumentException("Distance and distance unit must be provided together.");
        }

        if (instructions == null || instructions.isBlank()) {
            throw new IllegalArgumentException("Workout instructions are required.");
        }
    }
}