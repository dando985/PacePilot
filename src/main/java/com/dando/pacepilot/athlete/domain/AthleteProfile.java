package com.dando.pacepilot.athlete.domain;

import java.util.UUID;

public record AthleteProfile(
        UUID id,
        String displayName,
        ExperienceLevel experienceLevel,
        int availableTrainingDaysPerWeek,
        double currentWeeklyDistance,
        DistanceUnit distanceUnit,
        FitnessGoal goal
) {
}