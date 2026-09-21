package com.dando.pacepilot.athlete.service;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.api.CreateFitnessGoalRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AthleteProfileService {

    private final AthleteProfileRepository profileRepository;

    public AthleteProfileService(AthleteProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public AthleteProfile createProfile(CreateAthleteProfileRequest request) {
        CreateFitnessGoalRequest goalRequest = request.goal();

        FitnessGoal goal = new FitnessGoal(
                goalRequest.type(),
                goalRequest.targetDate(),
                goalRequest.targetTimeMinutes()
        );

        AthleteProfile profile = new AthleteProfile(
                UUID.randomUUID(),
                request.displayName(),
                request.experienceLevel(),
                request.availableTrainingDaysPerWeek(),
                request.currentWeeklyRunningDistance(),
                request.runningDistanceUnit(),
                goal
        );

        return profileRepository.save(profile);
    }

    public Optional<AthleteProfile> findProfileById(UUID id) {
        return profileRepository.findById(id);
    }

    public List<AthleteProfile> findAllProfiles() {
        return profileRepository.findAll();
    }
}