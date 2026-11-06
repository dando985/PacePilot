package com.dando.pacepilot.trainingplan.persistence;

import com.dando.pacepilot.trainingplan.domain.PlannedWorkout;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.domain.TrainingWeek;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class PostgresTrainingPlanRepository implements TrainingPlanRepository {

    private final TrainingPlanJpaRepository jpaRepository;

    public PostgresTrainingPlanRepository(TrainingPlanJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public TrainingPlan save(TrainingPlan plan) {
        TrainingPlanEntity entity = toEntity(plan);
        TrainingPlanEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<TrainingPlan> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<TrainingPlan> findByAthleteProfileId(UUID athleteProfileId) {
        return jpaRepository
                .findByAthleteProfileIdOrderByStartDateDescIdAsc(athleteProfileId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TrainingPlanEntity toEntity(TrainingPlan plan) {
        TrainingPlanEntity planEntity = new TrainingPlanEntity(
                plan.id(),
                plan.athleteProfileId(),
                plan.name(),
                plan.startDate(),
                plan.endDate()
        );

        for (TrainingWeek week : plan.weeks()) {
            TrainingWeekEntity weekEntity = new TrainingWeekEntity(planEntity, week.weekNumber(), week.startDate());

            for (int position = 0; position < week.workouts().size(); position++) {
                PlannedWorkout workout = week.workouts().get(position);

                PlannedWorkoutEntity workoutEntity = new PlannedWorkoutEntity(
                        workout.id(),
                        weekEntity,
                        position,
                        workout.scheduledDate(),
                        workout.type(),
                        workout.title(),
                        workout.targetDistance(),
                        workout.distanceUnit(),
                        workout.targetDurationMinutes(),
                        workout.instructions()
                );

                weekEntity.addWorkout(workoutEntity);
            }

            planEntity.addWeek(weekEntity);
        }

        return planEntity;
    }

    private TrainingPlan toDomain(TrainingPlanEntity entity) {
        List<TrainingWeek> weeks = entity.getWeeks().stream()
                .map(this::toDomainWeek)
                .toList();

        return new TrainingPlan(
                entity.getId(),
                entity.getAthleteProfileId(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                weeks
        );
    }

    private TrainingWeek toDomainWeek(TrainingWeekEntity entity) {
        List<PlannedWorkout> workouts = entity.getWorkouts().stream()
                .map(this::toDomainWorkout)
                .toList();

        return new TrainingWeek(
                entity.getId().getWeekNumber(),
                entity.getStartDate(),
                workouts
        );
    }

    private PlannedWorkout toDomainWorkout(PlannedWorkoutEntity entity) {
        return new PlannedWorkout(
                entity.getId(),
                entity.getScheduledDate(),
                entity.getType(),
                entity.getTitle(),
                entity.getTargetDistance(),
                entity.getDistanceUnit(),
                entity.getTargetDurationMinutes(),
                entity.getInstructions()
        );
    }
}