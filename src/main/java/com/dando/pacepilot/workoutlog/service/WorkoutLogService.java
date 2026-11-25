package com.dando.pacepilot.workoutlog.service;

import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.trainingplan.domain.PlannedWorkout;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.exception.PlannedWorkoutNotFoundException;
import com.dando.pacepilot.trainingplan.service.TrainingPlanService;
import com.dando.pacepilot.workoutlog.api.CreateWorkoutLogRequest;
import com.dando.pacepilot.workoutlog.api.UpdateWorkoutLogRequest;
import com.dando.pacepilot.workoutlog.domain.WorkoutLog;
import com.dando.pacepilot.workoutlog.exception.InvalidWorkoutLogException;
import com.dando.pacepilot.workoutlog.exception.WorkoutAlreadyLoggedException;
import com.dando.pacepilot.workoutlog.exception.WorkoutLogNotFoundException;
import com.dando.pacepilot.workoutlog.persistence.WorkoutLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Comparator;

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

    @Transactional
    public WorkoutLog updateLog(UUID logId, UpdateWorkoutLogRequest request) {
        if (logId == null || request == null) {
            throw new IllegalArgumentException("Workout log ID and update request are required.");
        }

        WorkoutLog existingLog = getLogById(logId);
        WorkoutLog updatedLog;

        try {
            updatedLog = new WorkoutLog(
                    existingLog.id(),
                    existingLog.athleteProfileId(),
                    existingLog.plannedWorkoutId(),
                    request.completedDate(),
                    request.actualDistance(),
                    request.distanceUnit(),
                    request.actualDurationMinutes(),
                    request.notes()
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidWorkoutLogException(exception);
        }

        return logRepository.save(updatedLog);
    }

    public WorkoutLog getLogById(UUID logId) {
        UUID athleteId = profileService.getCurrentProfile().id();

        return logRepository.findById(logId)
                .filter(log -> log.athleteProfileId().equals(athleteId))
                .orElseThrow(() -> new WorkoutLogNotFoundException(logId));
    }

    public List<WorkoutLog> findLogsByAthleteId(UUID athleteId) {
        // check profile ownership before querying logs
        profileService.getProfileById(athleteId);

        return logRepository.findByAthleteProfileId(athleteId);
    }

    /*
    Collects the IDs of every planned workout in the selected plan.
    Loads the athlete’s logs using existing repository query.
    Keeps logs whose athlete and planned workout match the selected plan.
     */
    public List<WorkoutLog> findLogsForPlan(TrainingPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Training plan is required.");
        }

        Set<UUID> workoutIds = plan.weeks().stream()
                .flatMap(week -> week.workouts().stream())
                .map(PlannedWorkout::id)
                .collect(Collectors.toSet());

        return findLogsByAthleteId(plan.athleteProfileId())
                .stream()
                .filter(log -> log.athleteProfileId().equals(plan.athleteProfileId()))
                .filter(log -> workoutIds.contains(log.plannedWorkoutId()))
                .toList();
    }

    public List<WorkoutLog> findLogsForAthleteBetween(UUID athleteId, LocalDate fromDate, LocalDate throughDate) {
        if (athleteId == null || fromDate == null || throughDate == null || throughDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("An athlete ID and valid workout history date range are required.");
        }

        return findLogsByAthleteId(athleteId).stream()
                .filter(log -> athleteId.equals(log.athleteProfileId()))
                .filter(log -> !log.completedDate().isBefore(fromDate) && !log.completedDate().isAfter(throughDate))
                .sorted(Comparator.comparing(WorkoutLog::completedDate).reversed().thenComparing(WorkoutLog::id))
                .toList();
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