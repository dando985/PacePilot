package com.dando.pacepilot.athlete.api;

import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.Gender;
import com.dando.pacepilot.shared.domain.WeightUnit;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAthleteProfileRequest(

        @NotBlank(message = "Display name is required")
        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        @NotNull(message = "Birth date is required")
        @PastOrPresent(message = "Birth date cannot be in the future")
        LocalDate birthDate,

        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be greater than zero")
        Double weight,

        @NotNull(message = "Weight unit is required")
        WeightUnit weightUnit,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotNull(message = "Experience level is required")
        ExperienceLevel experienceLevel
) {

    @AssertTrue(message = "Weight must be a finite number")
    public boolean isWeightFinite() {
        return weight == null || Double.isFinite(weight);
    }
}