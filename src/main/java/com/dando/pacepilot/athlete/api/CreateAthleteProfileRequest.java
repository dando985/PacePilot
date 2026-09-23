package com.dando.pacepilot.athlete.api;

import com.dando.pacepilot.athlete.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record CreateAthleteProfileRequest(

        @NotBlank(message = "Display name is required")
        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        @NotNull(message = "Experience level is required")
        ExperienceLevel experienceLevel,

        @NotNull(message = "Available training days are required")
        @Min(value = 1, message = "At least one training day is required")
        @Max(value = 7, message = "Training days cannot exceed seven")
        Integer availableTrainingDaysPerWeek,

        @PositiveOrZero(message = "Weekly running distance cannot be negative")
        Double currentWeeklyRunningDistance,

        DistanceUnit runningDistanceUnit,

        @NotNull(message = "Fitness goal is required")
        @Valid
        CreateFitnessGoalRequest goal
)
{

    // If either running-distance field is supplied, require both fields.
    @AssertTrue(
            message = """
                Running distance and distance unit must \
                either both be provided or both be omitted
                """
    )
    public boolean isRunningDistanceComplete() {
        return (currentWeeklyRunningDistance == null && runningDistanceUnit == null)
                ||
                (currentWeeklyRunningDistance != null && runningDistanceUnit != null);
    }
}