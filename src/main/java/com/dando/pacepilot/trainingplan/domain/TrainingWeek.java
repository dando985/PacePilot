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

        LocalDate weekEndDate = startDate.plusDays(6);

        for (PlannedWorkout workout : workouts) {
            if (workout == null) {
                throw new IllegalArgumentException("A workout cannot be null.");
            }

            LocalDate date = workout.scheduledDate();

            if (date.isBefore(startDate) || date.isAfter(weekEndDate)) {
                throw new IllegalArgumentException("Workout date must fall within its training week.");
            }
        }

        workouts = List.copyOf(workouts);
    }

    public LocalDate endDate() {
        return startDate.plusDays(6);
    }
}