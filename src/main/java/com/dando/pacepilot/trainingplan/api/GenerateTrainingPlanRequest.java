package com.dando.pacepilot.trainingplan.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/* Example of endpoint request body:
        {
        "name": "Two-week training plan",
        "startDate": "2027-01-04",
        "endDate": "2027-01-17"
        }
*/
public record GenerateTrainingPlanRequest(
        @NotBlank String name,
        @NotNull @FutureOrPresent LocalDate startDate,
        @NotNull LocalDate endDate
) {

    @AssertTrue(message = "End date cannot be before start date.")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }
}