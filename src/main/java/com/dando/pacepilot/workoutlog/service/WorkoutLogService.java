package com.dando.pacepilot.workoutlog.service;

import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.trainingplan.domain.PlannedWorkout;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.exception.PlannedWorkoutNotFoundException;
import com.dando.pacepilot.trainingplan.service.TrainingPlanService;
import com.dando.pacepilot.workoutlog.api.CreateWorkoutLogRequest;
import com.dando.pacepilot.workoutlog.domain.WorkoutLog;
import com.dando.pacepilot.workoutlog.exception.InvalidWorkoutLogException;
import com.dando.pacepilot.workoutlog.exception.WorkoutAlreadyLoggedException;
import com.dando.pacepilot.workoutlog.exception.WorkoutLogNotFoundException;
import com.dando.pacepilot.workoutlog.persistence.WorkoutLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Service for managing workout logs, including creating new logs and retrieving existing ones.
// It interacts with the WorkoutLogRepository for persistence, as well as the TrainingPlanService and AthleteProfileService for related data.
@Service
public class WorkoutLogService {

    private final WorkoutLogRepository logRepository;
    private final TrainingPlanService planService;
    private final AthleteProfileService profileService;

    public WorkoutLogService(WorkoutLogRepository logRepository, TrainingPlanService planService, AthleteProfileService profileService) {
        this.logRepository = logRepository;
        this.planService = planService;
        this.profileService = profileService;
    }

    public WorkoutLog createLog(UUID planId, UUID workoutId, CreateWorkoutLogRequest request) {
        if (planId == null || workoutId == null || request == null) {
            throw new IllegalArgumentException("Plan ID, workout ID, and completion request are required.");
        }

        TrainingPlan plan = planService.getPlanById(planId);
        PlannedWorkout workout = findPlannedWorkout(plan, workoutId);

        if (logRepository.findByPlannedWorkoutId(workout.id()).isPresent()) {
            throw new WorkoutAlreadyLoggedException(workout.id());
        }

        WorkoutLog log = toDomain(plan, workout, request);
        return logRepository.save(log);
    }

    public WorkoutLog getLogById(UUID logId) {
        return logRepository.findById(logId)
                .orElseThrow(() -> new WorkoutLogNotFoundException(logId));
    }

    public List<WorkoutLog> findLogsByAthleteId(UUID athleteId) {
        profileService.getProfileById(athleteId);
        return logRepository.findByAthleteProfileId(athleteId);
    }

    private PlannedWorkout findPlannedWorkout(TrainingPlan plan, UUID workoutId) {
        return plan.weeks().stream()
                .flatMap(week -> week.workouts().stream())
                .filter(workout -> workout.id().equals(workoutId))
                .findFirst()
                .orElseThrow(() -> new PlannedWorkoutNotFoundException(plan.id(), workoutId));
    }

    private WorkoutLog toDomain(TrainingPlan plan, PlannedWorkout workout, CreateWorkoutLogRequest request) {
        try {
            return new WorkoutLog(
                    UUID.randomUUID(),
                    plan.athleteProfileId(),
                    workout.id(),
                    request.completedDate(),
                    request.actualDistance(),
                    request.distanceUnit(),
                    request.actualDurationMinutes(),
                    request.notes()
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidWorkoutLogException(exception);
        }
    }
}