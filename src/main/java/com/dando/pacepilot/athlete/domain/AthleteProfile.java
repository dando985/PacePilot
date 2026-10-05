package com.dando.pacepilot.athlete.domain;

import com.dando.pacepilot.shared.domain.DistanceUnit;

import java.util.UUID;

public record AthleteProfile(
        UUID id,
        String displayName,
        ExperienceLevel experienceLevel,
        int availableTrainingDaysPerWeek,
        Double currentWeeklyRunningDistance,
        DistanceUnit runningDistanceUnit,
        FitnessGoal goal
) {
}