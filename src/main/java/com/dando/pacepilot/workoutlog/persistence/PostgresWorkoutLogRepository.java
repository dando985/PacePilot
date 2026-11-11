package com.dando.pacepilot.workoutlog.persistence;

import com.dando.pacepilot.workoutlog.domain.WorkoutLog;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class PostgresWorkoutLogRepository implements WorkoutLogRepository {

    private final WorkoutLogJpaRepository jpaRepository;

    public PostgresWorkoutLogRepository(WorkoutLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public WorkoutLog save(WorkoutLog log) {
        WorkoutLogEntity entity = toEntity(log);
        WorkoutLogEntity savedEntity = jpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<WorkoutLog> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<WorkoutLog> findByPlannedWorkoutId(UUID plannedWorkoutId) {
        return jpaRepository.findByPlannedWorkoutId(plannedWorkoutId)
                .map(this::toDomain);
    }

    @Override
    public List<WorkoutLog> findByAthleteProfileId(UUID athleteProfileId) {
        return jpaRepository
                .findByAthleteProfileIdOrderByCompletedDateDescIdAsc(athleteProfileId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private WorkoutLogEntity toEntity(WorkoutLog log) {
        return new WorkoutLogEntity(
                log.id(),
                log.athleteProfileId(),
                log.plannedWorkoutId(),
                log.completedDate(),
                log.actualDistance(),
                log.distanceUnit(),
                log.actualDurationMinutes(),
                log.notes()
        );
    }

    private WorkoutLog toDomain(WorkoutLogEntity entity) {
        return new WorkoutLog(
                entity.getId(),
                entity.getAthleteProfileId(),
                entity.getPlannedWorkoutId(),
                entity.getCompletedDate(),
                entity.getActualDistance(),
                entity.getDistanceUnit(),
                entity.getActualDurationMinutes(),
                entity.getNotes()
        );
    }
}