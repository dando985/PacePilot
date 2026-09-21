package com.dando.pacepilot.athlete.domain;

import java.time.LocalDate;

public record FitnessGoal(
        GoalType type,
        LocalDate targetDate,
        Integer targetTimeMinutes
) {
}