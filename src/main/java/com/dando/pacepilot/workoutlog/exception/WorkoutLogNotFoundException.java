package com.dando.pacepilot.workoutlog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkoutLogNotFoundException extends RuntimeException {

    public WorkoutLogNotFoundException(UUID logId) {
        super("Workout log was not found: " + logId);
    }
}