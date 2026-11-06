package com.dando.pacepilot.trainingplan.exception;

public class InvalidTrainingPlanException extends RuntimeException {

    public InvalidTrainingPlanException(IllegalArgumentException cause) {
        super(cause.getMessage(), cause);
    }
}