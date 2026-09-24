package com.dando.pacepilot.knowledge.exception;

public class TrainingDocumentLoadingException extends RuntimeException {

    public TrainingDocumentLoadingException(String message) {
        super(message);
    }

    public TrainingDocumentLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}