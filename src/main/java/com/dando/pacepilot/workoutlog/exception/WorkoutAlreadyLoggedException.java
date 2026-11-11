package com.dando.pacepilot.workoutlog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class WorkoutAlreadyLoggedException extends RuntimeException {

    public WorkoutAlreadyLoggedException(UUID plannedWorkoutId) {
        super("Planned workout already has a completion log: " + plannedWorkoutId);
    }
}