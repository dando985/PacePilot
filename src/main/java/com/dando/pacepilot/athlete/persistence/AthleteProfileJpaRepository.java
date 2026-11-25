package com.dando.pacepilot.athlete.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AthleteProfileJpaRepository extends JpaRepository<AthleteProfileEntity, UUID> {

    Optional<AthleteProfileEntity> findByIdAndOwnerAccountId(UUID id, UUID ownerAccountId);

    Optional<AthleteProfileEntity> findByOwnerAccountId(UUID ownerAccountId);
}