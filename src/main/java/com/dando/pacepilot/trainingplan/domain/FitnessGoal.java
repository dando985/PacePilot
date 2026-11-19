package com.dando.pacepilot.trainingplan.domain;

import java.time.LocalDate;

public record FitnessGoal(
        GoalType type,
        LocalDate targetDate,
        Integer targetTimeMinutes
) {
}