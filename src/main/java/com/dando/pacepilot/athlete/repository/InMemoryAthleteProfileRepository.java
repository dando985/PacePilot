package com.dando.pacepilot.athlete.repository;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAthleteProfileRepository implements AthleteProfileRepository {

    private final Map<UUID, AthleteProfile> profiles = new ConcurrentHashMap<>();

    @Override
    public AthleteProfile save(AthleteProfile profile) {
        profiles.put(profile.id(), profile);
        return profile;
    }

    @Override
    public Optional<AthleteProfile> findById(UUID id) {
        return Optional.ofNullable(profiles.get(id));
    }

    @Override
    public List<AthleteProfile> findAll() {
        return List.copyOf(profiles.values());
    }
}