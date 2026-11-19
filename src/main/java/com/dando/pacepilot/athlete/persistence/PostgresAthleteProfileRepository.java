package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.trainingplan.domain.FitnessGoal;
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
        return new AthleteProfileEntity(
                profile.id(),
                profile.displayName(),
                profile.birthDate(),
                profile.weight(),
                profile.weightUnit(),
                profile.gender(),
                profile.experienceLevel()
        );
    }

    // helper to convert AthleteProfileEntity to AthleteProfile for application side use
    private AthleteProfile toDomain(AthleteProfileEntity entity) {
        return new AthleteProfile(
                entity.getId(),
                entity.getDisplayName(),
                entity.getBirthDate(),
                entity.getWeight(),
                entity.getWeightUnit(),
                entity.getGender(),
                entity.getExperienceLevel()
        );
    }
}