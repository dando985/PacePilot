package com.dando.pacepilot.workoutlog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutLogJpaRepository extends JpaRepository<WorkoutLogEntity, UUID> {

    // Conceptually means:
    // SELECT * FROM workout_logs WHERE planned_workout_id = ?
    Optional<WorkoutLogEntity> findByPlannedWorkoutId(UUID plannedWorkoutId);

    // Conceptually means:
    // SELECT * FROM workout_logs WHERE athlete_profile_id = ? ORDER BY completed_date DESC, id ASC
    List<WorkoutLogEntity> findByAthleteProfileIdOrderByCompletedDateDescIdAsc(UUID athleteProfileId);
}