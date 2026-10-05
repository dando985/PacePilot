package com.dando.pacepilot.trainingplan.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TrainingPlan(
        UUID id,
        UUID athleteProfileId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
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

        if (weeks == null || weeks.isEmpty()) {
            throw new IllegalArgumentException("A training plan must contain weeks.");
        }

        weeks = List.copyOf(weeks);
    }
}