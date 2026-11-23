package com.dando.pacepilot.trainingplan.domain;

import com.dando.pacepilot.shared.domain.DistanceUnit;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record TrainingPlan(
        UUID id,
        UUID athleteProfileId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        FitnessGoal goal,
        int availableTrainingDaysPerWeek,
        DistanceUnit distanceUnit,
        boolean archived,
        List<TrainingWeek> weeks
) {

    public TrainingPlan {
        if (id == null) {
            throw new IllegalArgumentException("Training plan ID is required.");
        }

        if (athleteProfileId == null) {
            throw new IllegalArgumentException("Athlete profile ID is required.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Training plan name is required.");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Plan dates are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        if (goal == null || goal.type() == null || goal.targetDate() == null) {
            throw new IllegalArgumentException("A goal with a type and target date is required.");
        }

        if (goal.targetTimeMinutes() != null && goal.targetTimeMinutes() <= 0) {
            throw new IllegalArgumentException("Goal target time must be greater than zero.");
        }

        if (availableTrainingDaysPerWeek < 1 || availableTrainingDaysPerWeek > 7) {
            throw new IllegalArgumentException("Available training days must be between 1 and 7.");
        }

        if (distanceUnit == null) {
            throw new IllegalArgumentException("Plan distance unit is required.");
        }

        if (weeks == null || weeks.isEmpty()) {
            throw new IllegalArgumentException("A training plan must contain weeks.");
        }

        long requiredWeeks = ChronoUnit.DAYS.between(startDate, endDate) / 7 + 1;

        if (weeks.size() != requiredWeeks) {
            throw new IllegalArgumentException("Week count must match the plan's date range.");
        }

        Set<UUID> workoutIds = new HashSet<>();

        for (int index = 0; index < weeks.size(); index++) {
            TrainingWeek week = weeks.get(index);

            if (week == null) {
                throw new IllegalArgumentException("A training week cannot be null.");
            }

            if (week.weekNumber() != index + 1) {
                throw new IllegalArgumentException("Weeks must be ordered and numbered consecutively from 1.");
            }

            LocalDate expectedStartDate = startDate.plusWeeks(index);

            if (!week.startDate().equals(expectedStartDate)) {
                throw new IllegalArgumentException("Each week must start seven days after the previous week, "
                        + "with week 1 starting on the plan's start date."
                );
            }

            long trainingDays = week.workouts().stream()
                    .filter(workout -> workout.type() != WorkoutType.REST)
                    .map(PlannedWorkout::scheduledDate)
                    .distinct()
                    .count();

            if (trainingDays > availableTrainingDaysPerWeek) {
                throw new IllegalArgumentException("Week %d exceeds the plan's %d available training days."
                        .formatted(week.weekNumber(), availableTrainingDaysPerWeek)
                );
            }

            for (PlannedWorkout workout : week.workouts()) {
                if (workout.scheduledDate().isAfter(endDate)) {
                    throw new IllegalArgumentException("Workout date cannot be after the plan's end date.");
                }

                if (!workoutIds.add(workout.id())) {
                    throw new IllegalArgumentException("Workout IDs must be unique within a plan.");
                }
            }
        }

        weeks = List.copyOf(weeks);
    }

    public TrainingPlan(
            UUID id,
            UUID athleteProfileId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            FitnessGoal goal,
            int availableTrainingDaysPerWeek,
            DistanceUnit distanceUnit,
            List<TrainingWeek> weeks
    ) {
        this(
                id,
                athleteProfileId,
                name,
                startDate,
                endDate,
                goal,
                availableTrainingDaysPerWeek,
                distanceUnit,
                false,
                weeks
        );
    }
}