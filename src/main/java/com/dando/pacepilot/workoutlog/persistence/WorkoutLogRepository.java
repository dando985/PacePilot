package com.dando.pacepilot.workoutlog.persistence;

import com.dando.pacepilot.workoutlog.domain.WorkoutLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutLogRepository {

    WorkoutLog save(WorkoutLog log);

    Optional<WorkoutLog> findById(UUID id);

    Optional<WorkoutLog> findByPlannedWorkoutId(UUID plannedWorkoutId);

    List<WorkoutLog> findByAthleteProfileId(UUID athleteProfileId);
}