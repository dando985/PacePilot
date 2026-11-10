package com.dando.pacepilot.trainingplan.generation;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Builds a JSON schema for the training plan structure, which can be used for validation and documentation purposes.
@Component
public class TrainingPlanJsonSchema {

    public Map<String, Object> schema() {
        return objectSchema(
                Map.of(
                        "name", stringSchema(),
                        "startDate", dateSchema(),
                        "endDate", dateSchema(),
                        "weeks", arraySchema(weekSchema())
                ),
                List.of("name", "startDate", "endDate", "weeks")
        );
    }

    private Map<String, Object> weekSchema() {
        return objectSchema(
                Map.of(
                        "weekNumber", Map.of(
                                "type", "integer",
                                "minimum", 1
                        ),
                        "startDate", dateSchema(),
                        "workouts", arraySchema(workoutSchema())
                ),
                List.of("weekNumber", "startDate", "workouts")
        );
    }

    private Map<String, Object> workoutSchema() {
        return objectSchema(
                Map.of(
                        "scheduledDate", dateSchema(),
                        "type", enumSchema(WorkoutType.values()),
                        "title", stringSchema(),
                        "targetDistance", nullableSchema(
                                Map.of("type", "number", "exclusiveMinimum", 0)
                        ),
                        "distanceUnit", nullableSchema(
                                enumSchema(DistanceUnit.values())
                        ),
                        "targetDurationMinutes", nullableSchema(
                                Map.of("type", "integer", "minimum", 1)
                        ),
                        "instructions", stringSchema()
                ),
                List.of(
                        "scheduledDate",
                        "type",
                        "title",
                        "targetDistance",
                        "distanceUnit",
                        "targetDurationMinutes",
                        "instructions"
                )
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

    private Map<String, Object> arraySchema(Map<String, Object> items) {
        return Map.of(
                "type", "array",
                "items", items,
                "minItems", 1
        );
    }

    private Map<String, Object> stringSchema() {
        return Map.of(
                "type", "string",
                "minLength", 1
        );
    }

    private Map<String, Object> dateSchema() {
        return Map.of(
                "type", "string",
                "format", "date"
        );
    }

    private Map<String, Object> nullableSchema(Map<String, Object> valueSchema) {
        return Map.of(
                "anyOf", List.of(valueSchema, Map.of("type", "null"))
        );
    }

    private Map<String, Object> enumSchema(Enum<?>[] values) {
        List<String> names = Arrays.stream(values)
                .map(Enum::name)
                .toList();

        return Map.of("type", "string", "enum", names);
    }
}