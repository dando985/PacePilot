package com.dando.pacepilot.trainingplan.domain;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingPlanModelTest {

    private static final LocalDate START =
            LocalDate.of(2030, 1, 1);

    @Test
    void acceptsPartialFinalWeek() {
        TrainingWeek first = week(1, START);

        TrainingWeek second = new TrainingWeek(
                2,
                START.plusDays(7),
                List.of(workout(START.plusDays(9)))
        );

        TrainingPlan plan = plan(
                START.plusDays(9),
                List.of(first, second)
        );

        assertEquals(2, plan.weeks().size());
        assertEquals(plan.endDate(), plan.weeks().get(1).workouts().get(0).scheduledDate());
    }

    @Test
    void rejectsInvalidDistances() {
        double[] invalidDistances = {
                0.0,
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double distance : invalidDistances) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new PlannedWorkout(
                            UUID.randomUUID(),
                            START,
                            WorkoutType.WALK,
                            "Example walk",
                            distance,
                            DistanceUnit.KILOMETERS,
                            null,
                            "Example instructions."
                    )
            );
        }
    }

    @Test
    void rejectsWorkoutsOutsideTheirWeek() {
        for (int offset : new int[]{-1, 7}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TrainingWeek(
                            1,
                            START,
                            List.of(workout(START.plusDays(offset)))
                    )
            );
        }
    }

    @Test
    void rejectsDuplicateOrSkippedWeekNumbers() {
        for (int secondNumber : new int[]{1, 3}) {
            List<TrainingWeek> weeks = List.of(
                    week(1, START),
                    week(secondNumber, START.plusDays(7))
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> plan(START.plusDays(13), weeks)
            );
        }
    }

    @Test
    void rejectsIncorrectWeekStartDate() {
        List<TrainingWeek> weeks = List.of(
                week(1, START),
                week(2, START.plusDays(8))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> plan(START.plusDays(13), weeks)
        );
    }

    @Test
    void rejectsMissingWeek() {
        assertThrows(
                IllegalArgumentException.class,
                () -> plan(
                        START.plusDays(13),
                        List.of(week(1, START))
                )
        );
    }

    @Test
    void rejectsWorkoutAfterPlanEndDate() {
        TrainingWeek second = new TrainingWeek(
                2,
                START.plusDays(7),
                List.of(workout(START.plusDays(10)))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> plan(
                        START.plusDays(9),
                        List.of(week(1, START), second)
                )
        );
    }

    @Test
    void rejectsWorkoutIdReusedAcrossWeeks() {
        PlannedWorkout first = workout(START);

        PlannedWorkout second = new PlannedWorkout(
                first.id(),
                START.plusDays(7),
                WorkoutType.WALK,
                "Another walk",
                null,
                null,
                20,
                "Example instructions."
        );

        List<TrainingWeek> weeks = List.of(
                new TrainingWeek(1, START, List.of(first)),
                new TrainingWeek(
                        2,
                        START.plusDays(7),
                        List.of(second)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> plan(START.plusDays(13), weeks)
        );
    }

    @Test
    void keepsIndependentUnmodifiableLists() {
        List<PlannedWorkout> originalWorkouts = new ArrayList<>(List.of(workout(START)));

        TrainingWeek week = new TrainingWeek(
                1,
                START,
                originalWorkouts
        );

        List<TrainingWeek> originalWeeks =
                new ArrayList<>(List.of(week));

        TrainingPlan plan = plan(
                START.plusDays(6),
                originalWeeks
        );

        originalWorkouts.clear();
        originalWeeks.clear();

        assertEquals(1, plan.weeks().size());
        assertEquals(1, plan.weeks().get(0).workouts().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.weeks().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> week.workouts().clear()
        );
    }

    private PlannedWorkout workout(LocalDate date) {
        return new PlannedWorkout(
                UUID.randomUUID(),
                date,
                WorkoutType.WALK,
                "Example walk",
                null,
                null,
                20,
                "Example instructions."
        );
    }

    // helper method to create a TrainingWeek with a single workout on the start date
    private TrainingWeek week(int number, LocalDate startDate) {
        return new TrainingWeek(
                number,
                startDate,
                List.of(workout(startDate))
        );
    }

    // helper method to create a TrainingPlan with the specified end date and weeks
    private TrainingPlan plan(LocalDate endDate, List<TrainingWeek> weeks) {
        return new TrainingPlan(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Example plan",
                START,
                endDate,
                weeks
        );
    }
}