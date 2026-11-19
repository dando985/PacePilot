package com.dando.pacepilot.trainingplan.generation;

import com.dando.pacepilot.generation.TextGenerationException;
import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

// Parses and validates the active workout choices returned by Ollama.
@Component
public class TrainingPlanGenerationParser {

    private final ObjectReader workoutReader;

    public TrainingPlanGenerationParser(JsonMapper jsonMapper) {
        this.workoutReader = jsonMapper.rebuild()
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .build()
                .readerFor(WorkoutResponse.class)
                .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .with(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .without(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    }

    public List<GeneratedWorkout> parseWorkouts(String json, int expectedCount, int maxDurationMinutes, int maxDistanceMeters) {
        if (expectedCount < 0 || maxDurationMinutes < 1 || maxDistanceMeters < 1) {
            throw new IllegalArgumentException("A valid workout count and positive target limits are required.");
        }

        if (json == null || json.isBlank()) {
            throw new TextGenerationException("Ollama returned no workout JSON.");
        }

        WorkoutResponse response;

        try {
            response = workoutReader.readValue(json);
        } catch (JacksonException exception) {
            throw new TextGenerationException("Ollama returned invalid workout JSON: " + exception.getMessage(), exception);
        }

        if (response == null || response.workouts() == null) {
            throw new TextGenerationException("Ollama returned no workout list.");
        }

        if (response.workouts().size() != expectedCount) {
            throw new TextGenerationException("Expected %d workouts, but Ollama returned %d.".formatted(expectedCount, response.workouts().size()));
        }

        for (int index = 0; index < response.workouts().size(); index++) {
            validateWorkout(response.workouts().get(index), index, maxDurationMinutes, maxDistanceMeters);
        }

        return List.copyOf(response.workouts());
    }

    private void validateWorkout(GeneratedWorkout workout, int index, int maxDurationMinutes, int maxDistanceMeters) {
        String location = "workouts[" + index + "]";

        if (workout == null
                || workout.type() == null
                || workout.targetKind() == null
                || workout.targetValue() == null) {
            throw new TextGenerationException(location + " must contain type, targetKind, and targetValue.");
        }

        if (workout.type() == WorkoutType.REST) {
            throw new TextGenerationException(location + ": REST days are assigned by Java.");
        }

        if (workout.targetKind() == TargetKind.DISTANCE_METERS
                && (workout.type() == WorkoutType.STRENGTH || workout.type() == WorkoutType.CROSS_TRAINING)) {
            throw new TextGenerationException(location + ": " + workout.type() + " requires DURATION_MINUTES.");
        }

        int maximum = workout.targetKind() == TargetKind.DURATION_MINUTES ? maxDurationMinutes : maxDistanceMeters;

        if (workout.targetValue() < 1 || workout.targetValue() > maximum) {
            throw new TextGenerationException("%s.targetValue must be between 1 and %d for %s; received %d."
                            .formatted(location, maximum, workout.targetKind(), workout.targetValue())
            );
        }
    }

    public record GeneratedWorkout(
            WorkoutType type,
            TargetKind targetKind,
            Integer targetValue
    ) {
    }

    public enum TargetKind {
        DURATION_MINUTES,
        DISTANCE_METERS
    }

    private record WorkoutResponse(
            List<GeneratedWorkout> workouts
    ) {
    }
}