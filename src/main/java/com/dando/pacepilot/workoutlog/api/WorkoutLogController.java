package com.dando.pacepilot.workoutlog.api;

import com.dando.pacepilot.workoutlog.domain.WorkoutLog;
import com.dando.pacepilot.workoutlog.exception.InvalidWorkoutLogException;
import com.dando.pacepilot.workoutlog.exception.WorkoutAlreadyLoggedException;
import com.dando.pacepilot.workoutlog.service.WorkoutLogService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class WorkoutLogController {

    private final WorkoutLogService logService;

    public WorkoutLogController(WorkoutLogService logService) {
        this.logService = logService;
    }

    @PostMapping("/api/training-plans/{planId}/workouts/{workoutId}/logs")
    public ResponseEntity<WorkoutLog> createLog(
            @PathVariable UUID planId,
            @PathVariable UUID workoutId,
            @Valid @RequestBody CreateWorkoutLogRequest request
    ) {
        // Create a new workout log and save it in the database
        WorkoutLog log = logService.createLog(planId, workoutId, request);

        // Return a response with the location of the newly created log
        URI location = URI.create("/api/workout-logs/" + log.id());
        return ResponseEntity.created(location).body(log);
    }

    @GetMapping("/api/workout-logs/{logId}")
    public WorkoutLog getLogById(@PathVariable UUID logId) {
        return logService.getLogById(logId);
    }

    @GetMapping("/api/athletes/{athleteId}/workout-logs")
    public List<WorkoutLog> findLogsByAthleteId(@PathVariable UUID athleteId) {
        return logService.findLogsByAthleteId(athleteId);
    }

    @ExceptionHandler(InvalidWorkoutLogException.class)
    public ProblemDetail handleInvalidLog(InvalidWorkoutLogException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(WorkoutAlreadyLoggedException.class)
    public ProblemDetail handleAlreadyLogged(WorkoutAlreadyLoggedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handlePersistenceConflict() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The workout log could not be saved because it conflicts with the current stored data.");
    }
}