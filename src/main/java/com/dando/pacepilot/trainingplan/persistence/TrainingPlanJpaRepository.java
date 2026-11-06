package com.dando.pacepilot.trainingplan.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrainingPlanJpaRepository extends JpaRepository<TrainingPlanEntity, UUID> {

    // Implemented automatically by Spring Data JPA based on method name
    // Interpreted as:  SELECT *
    //                  FROM training_plans
    //                  WHERE athlete_profile_id = ?
    //                  ORDER BY start_date DESC, id ASC;
    List<TrainingPlanEntity> findByAthleteProfileIdOrderByStartDateDescIdAsc(UUID athleteProfileId);
}