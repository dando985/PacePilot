package com.dando.pacepilot.trainingplan.domain;

import java.time.LocalDate;
import java.util.List;

public record TrainingWeek(
        int weekNumber,
        LocalDate startDate,
        List<PlannedWorkout> workouts
) {

    public TrainingWeek {
        if (weekNumber < 1) {
            throw new IllegalArgumentException("Week number must be at least 1.");
        }

        if (startDate == null) {
            throw new IllegalArgumentException("Week start date is required.");
        }

        if (workouts == null || workouts.isEmpty()) {
            throw new IllegalArgumentException("A training week must contain workouts.");
        }

        workouts = List.copyOf(workouts);
    }

    public LocalDate endDate() {
        return startDate.plusDays(6);
    }
}