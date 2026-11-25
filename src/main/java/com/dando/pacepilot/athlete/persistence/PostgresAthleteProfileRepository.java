package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresAthleteProfileRepository implements AthleteProfileRepository {

    private final AthleteProfileJpaRepository jpaRepository;

    public PostgresAthleteProfileRepository(AthleteProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AthleteProfile save(AthleteProfile profile, UUID ownerAccountId) {
        AthleteProfileEntity entity = toEntity(profile, ownerAccountId);
        AthleteProfileEntity savedEntity = jpaRepository.saveAndFlush(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<AthleteProfile> findByIdAndOwnerAccountId(UUID id, UUID ownerAccountId) {
        return jpaRepository.findByIdAndOwnerAccountId(id, ownerAccountId).map(this::toDomain);
    }

    @Override
    public Optional<AthleteProfile> findByOwnerAccountId(UUID ownerAccountId) {
        return jpaRepository.findByOwnerAccountId(ownerAccountId).map(this::toDomain);
    }

    private AthleteProfileEntity toEntity(AthleteProfile profile, UUID ownerAccountId) {
        return new AthleteProfileEntity(
                profile.id(),
                ownerAccountId,
                profile.displayName(),
                profile.birthDate(),
                profile.weight(),
                profile.weightUnit(),
                profile.gender(),
                profile.experienceLevel()
        );
    }

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