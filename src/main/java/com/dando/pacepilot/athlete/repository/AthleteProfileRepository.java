package com.dando.pacepilot.athlete.repository;

import com.dando.pacepilot.athlete.domain.AthleteProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AthleteProfileRepository {

    AthleteProfile save(AthleteProfile profile);

    Optional<AthleteProfile> findById(UUID id);

    List<AthleteProfile> findAll();
}