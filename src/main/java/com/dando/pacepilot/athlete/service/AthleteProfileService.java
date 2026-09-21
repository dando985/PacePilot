package com.dando.pacepilot.athlete.service;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.api.CreateFitnessGoalRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AthleteProfileService {

    public AthleteProfile createProfile(CreateAthleteProfileRequest request) {
        CreateFitnessGoalRequest goalRequest = request.goal();

        FitnessGoal goal = new FitnessGoal(
                goalRequest.type(),
                goalRequest.targetDate(),
                goalRequest.targetTimeMinutes()
        );

        return new AthleteProfile(
                UUID.randomUUID(),
                request.displayName(),
                request.experienceLevel(),
                request.availableTrainingDaysPerWeek(),
                request.currentWeeklyRunningDistance(),
                request.runningDistanceUnit(),
                goal
        );
    }
}