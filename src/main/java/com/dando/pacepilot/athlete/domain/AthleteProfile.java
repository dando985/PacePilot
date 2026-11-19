package com.dando.pacepilot.athlete.domain;

import com.dando.pacepilot.shared.domain.WeightUnit;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

public record AthleteProfile(
        UUID id,
        String displayName,
        LocalDate birthDate,
        Double weight,
        WeightUnit weightUnit,
        Gender gender,
        ExperienceLevel experienceLevel
) {

    public Integer ageOn(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Age calculation date is required.");
        }

        if (birthDate == null) {
            return null;
        }

        if (date.isBefore(birthDate)) {
            throw new IllegalArgumentException("Age calculation date cannot be before birth date.");
        }

        return Period.between(birthDate, date).getYears();
    }
}