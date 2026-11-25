package com.dando.pacepilot.athlete.repository;

import com.dando.pacepilot.athlete.domain.AthleteProfile;

import java.util.Optional;
import java.util.UUID;

public interface AthleteProfileRepository {

    AthleteProfile save(AthleteProfile profile, UUID ownerAccountId);

    Optional<AthleteProfile> findByIdAndOwnerAccountId(UUID id, UUID ownerAccountId);

    Optional<AthleteProfile> findByOwnerAccountId(UUID ownerAccountId);
}