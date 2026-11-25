package com.dando.pacepilot.trainingplan.service;

import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.trainingplan.api.CreatePlannedWorkoutRequest;
import com.dando.pacepilot.trainingplan.api.CreateTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.api.CreateTrainingWeekRequest;
import com.dando.pacepilot.trainingplan.domain.PlannedWorkout;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.domain.TrainingWeek;
import com.dando.pacepilot.trainingplan.exception.InvalidTrainingPlanException;
import com.dando.pacepilot.trainingplan.exception.TrainingPlanNotFoundException;
import com.dando.pacepilot.trainingplan.persistence.TrainingPlanRepository;
import com.dando.pacepilot.trainingplan.domain.FitnessGoal;
import com.dando.pacepilot.trainingplan.api.RenameTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.api.UpdateTrainingPlanArchiveRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository planRepository;
    private final AthleteProfileService profileService;

    public TrainingPlanService(TrainingPlanRepository planRepository, AthleteProfileService profileService) {
        this.planRepository = planRepository;
        this.profileService = profileService;
    }

    public TrainingPlan createPlan(UUID athleteId, CreateTrainingPlanRequest request) {
        // check that athlete exists before creating a plan for them
        profileService.getProfileById(athleteId);

        // create the plan and save it to the repository
        TrainingPlan plan = toDomain(athleteId, request);
        return planRepository.save(plan);
    }

    public TrainingPlan renamePlan(UUID planId, RenameTrainingPlanRequest request) {
        // verify athlete profile ownership of plan before rename
        getPlanById(planId);

        return planRepository.rename(planId, request.name()).orElseThrow(() -> new TrainingPlanNotFoundException(planId));
    }

    public TrainingPlan setArchived(UUID planId, UpdateTrainingPlanArchiveRequest request) {
        // verify athlete profile ownership of plan before archiving
        getPlanById(planId);

        return planRepository.setArchived(planId, request.archived()).orElseThrow(() -> new TrainingPlanNotFoundException(planId));
    }

    public TrainingPlan getPlanById(UUID planId) {
        UUID athleteId = profileService.getCurrentProfile().id();

        return planRepository.findById(planId)
                .filter(plan -> plan.athleteProfileId().equals(athleteId))
                .orElseThrow(() -> new TrainingPlanNotFoundException(planId));
    }

    public List<TrainingPlan> findPlansByAthleteId(UUID athleteId) {
        // check that athlete exists before retrieving their plans
        profileService.getProfileById(athleteId);

        return planRepository.findByAthleteProfileId(athleteId);
    }

    // Private helper methods to convert API request objects to domain objects
    private TrainingPlan toDomain(UUID athleteId, CreateTrainingPlanRequest request) {
        try {
            FitnessGoal goal = new FitnessGoal(
                    request.goal().type(),
                    request.goal().targetDate(),
                    request.goal().targetTimeMinutes()
            );

            // convert each CreateTrainingWeekRequest to a TrainingWeek
            List<TrainingWeek> weeks = request.weeks().stream().map(this::toDomainWeek).toList();

            return new TrainingPlan(
                    UUID.randomUUID(),
                    athleteId,
                    request.name(),
                    request.startDate(),
                    request.endDate(),
                    goal,
                    request.availableTrainingDaysPerWeek(),
                    request.distanceUnit(),
                    weeks
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTrainingPlanException(exception);
        }
    }

    private TrainingWeek toDomainWeek(CreateTrainingWeekRequest request) {
        List<PlannedWorkout> workouts = request.workouts().stream().map(this::toDomainWorkout).toList();

        return new TrainingWeek(request.weekNumber(), request.startDate(), workouts);
    }

    private PlannedWorkout toDomainWorkout(CreatePlannedWorkoutRequest request) {
        return new PlannedWorkout(
                UUID.randomUUID(),
                request.scheduledDate(),
                request.type(),
                request.title(),
                request.targetDistance(),
                request.distanceUnit(),
                request.targetDurationMinutes(),
                request.instructions()
        );
    }
}