package com.dando.pacepilot.workoutlog.exception;

public class InvalidWorkoutLogException extends RuntimeException {

    public InvalidWorkoutLogException(IllegalArgumentException cause) {
        super(cause.getMessage(), cause);
    }
}