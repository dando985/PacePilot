package com.dando.pacepilot.trainingplan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlannedWorkoutNotFoundException extends RuntimeException {

    public PlannedWorkoutNotFoundException(UUID planId, UUID workoutId) {
        super("Planned workout " + workoutId + " was not found in training plan: " + planId);
    }
}