package com.dando.pacepilot.trainingplan.persistence;

import com.dando.pacepilot.trainingplan.domain.TrainingPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingPlanRepository {

    TrainingPlan save(TrainingPlan plan);

    Optional<TrainingPlan> rename(UUID planId, String newName);

    Optional<TrainingPlan> setArchived(UUID planId, boolean archived);

    Optional<TrainingPlan> findById(UUID id);

    List<TrainingPlan> findByAthleteProfileId(UUID athleteProfileId);
}