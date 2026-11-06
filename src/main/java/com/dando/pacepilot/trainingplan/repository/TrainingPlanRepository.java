package com.dando.pacepilot.trainingplan.repository;

import com.dando.pacepilot.trainingplan.domain.TrainingPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingPlanRepository {

    TrainingPlan save(TrainingPlan plan);

    Optional<TrainingPlan> findById(UUID id);

    List<TrainingPlan> findByAthleteProfileId(UUID athleteProfileId);
}