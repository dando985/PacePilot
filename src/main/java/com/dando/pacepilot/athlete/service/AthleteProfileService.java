package com.dando.pacepilot.athlete.service;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.exception.AthleteProfileNotFoundException;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AthleteProfileService {

    private final AthleteProfileRepository profileRepository;

    public AthleteProfileService(AthleteProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public AthleteProfile createProfile(CreateAthleteProfileRequest request) {
        AthleteProfile profile = new AthleteProfile(
                UUID.randomUUID(),
                request.displayName(),
                request.birthDate(),
                request.weight(),
                request.weightUnit(),
                request.gender(),
                request.experienceLevel()
        );

        return profileRepository.save(profile);
    }

    public AthleteProfile getProfileById(UUID id) {
        return profileRepository.findById(id).orElseThrow(() -> new AthleteProfileNotFoundException(id));
    }

    public List<AthleteProfile> findAllProfiles() {
        return profileRepository.findAll();
    }
}