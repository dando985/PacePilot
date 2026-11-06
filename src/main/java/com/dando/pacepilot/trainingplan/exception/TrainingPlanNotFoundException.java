package com.dando.pacepilot.trainingplan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrainingPlanNotFoundException extends RuntimeException {

    public TrainingPlanNotFoundException(UUID planId) {
        super("Training plan was not found: " + planId);
    }
}