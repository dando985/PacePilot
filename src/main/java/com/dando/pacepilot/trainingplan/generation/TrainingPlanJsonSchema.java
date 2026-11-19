package com.dando.pacepilot.trainingplan.generation;

import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Defines the active workout choices Ollama returns.
@Component
public class TrainingPlanJsonSchema {

    public Map<String, Object> schema(int workoutCount, int maxDurationMinutes, int maxDistanceMeters) {
        if (workoutCount < 0) {
            throw new IllegalArgumentException("Workout count cannot be negative.");
        }

        if (maxDurationMinutes < 1 || maxDistanceMeters < 1) {
            throw new IllegalArgumentException("Workout target limits must be positive.");
        }

        WorkoutType[] activeTypes = Arrays.stream(WorkoutType.values())
                .filter(type -> type != WorkoutType.REST)
                .toArray(WorkoutType[]::new);

        WorkoutType[] distanceTypes = {
                WorkoutType.WALK,
                WorkoutType.EASY_RUN,
                WorkoutType.RECOVERY_RUN,
                WorkoutType.LONG_RUN,
                WorkoutType.TEMPO_RUN,
                WorkoutType.INTERVALS,
                WorkoutType.RACE
        };

        Map<String, Object> workout = Map.of(
                "anyOf", List.of(
                        workoutVariantSchema(activeTypes, "DURATION_MINUTES", maxDurationMinutes),
                        workoutVariantSchema(distanceTypes, "DISTANCE_METERS", maxDistanceMeters)
                )
        );

        Map<String, Object> workouts = Map.of(
                "type", "array",
                "items", workout,
                "minItems", workoutCount,
                "maxItems", workoutCount
        );

        return objectSchema(Map.of("workouts", workouts), List.of("workouts"));
    }

    private Map<String, Object> workoutVariantSchema(WorkoutType[] types, String targetKind, int maximum) {
        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("type", enumSchema(types));

        properties.put(
                "targetKind",
                Map.of(
                        "type", "string",
                        "enum", List.of(targetKind)
                )
        );

        properties.put(
                "targetValue",
                Map.of(
                        "type", "integer",
                        "minimum", 1,
                        "maximum", maximum
                )
        );

        // Includes any additional comments AI may provide to the athlete about the workout, such as pacing or form tips.
        properties.put(
                "coachingComment",
                Map.of(
                        "type", "string",
                        "maxLength", 240
                )
        );


        return objectSchema(properties, List.of("type", "targetKind", "targetValue", "coachingComment")
        );
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", required,
                "additionalProperties", false
        );
    }

    private Map<String, Object> enumSchema(Enum<?>[] values) {
        List<String> names = Arrays.stream(values).map(Enum::name).toList();

        return Map.of(
                "type", "string",
                "enum", names
        );
    }
}