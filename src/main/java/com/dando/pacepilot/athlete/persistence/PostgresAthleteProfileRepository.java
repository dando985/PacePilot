package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresAthleteProfileRepository implements AthleteProfileRepository {

    private final AthleteProfileJpaRepository jpaRepository;

    public PostgresAthleteProfileRepository(AthleteProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AthleteProfile save(AthleteProfile profile) {
        AthleteProfileEntity entity = toEntity(profile);

        AthleteProfileEntity savedEntity = jpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<AthleteProfile> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AthleteProfile> findAll() {
        return jpaRepository
                .findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // helper to convert AthleteProfile to AthleteProfileEntity for database side use
    private AthleteProfileEntity toEntity(AthleteProfile profile) {
        FitnessGoal goal = profile.goal();

        return new AthleteProfileEntity(
                profile.id(),
                profile.displayName(),
                profile.experienceLevel(),
                profile.availableTrainingDaysPerWeek(),
                profile.currentWeeklyRunningDistance(),
                profile.runningDistanceUnit(),
                goal.type(),
                goal.targetDate(),
                goal.targetTimeMinutes()
        );
    }

    // helper to convert AthleteProfileEntity to AthleteProfile for application side use
    private AthleteProfile toDomain(AthleteProfileEntity entity) {
        FitnessGoal goal = new FitnessGoal(
                entity.getGoalType(),
                entity.getTargetDate(),
                entity.getTargetTimeMinutes()
        );

        return new AthleteProfile(
                entity.getId(),
                entity.getDisplayName(),
                entity.getExperienceLevel(),
                entity.getAvailableTrainingDaysPerWeek(),
                entity.getCurrentWeeklyRunningDistance(),
                entity.getRunningDistanceUnit(),
                goal
        );
    }
}