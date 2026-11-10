package com.dando.pacepilot.trainingplan.generation;

import com.dando.pacepilot.generation.TextGenerationException;
import com.dando.pacepilot.trainingplan.api.CreateTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.api.GenerateTrainingPlanRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.stream.Collectors;

// Converts Ollama-generated JSON into a CreateTrainingPlanRequest object
@Component
public class TrainingPlanGenerationParser {

    private final ObjectReader planReader;
    private final Validator validator;

    public TrainingPlanGenerationParser(JsonMapper jsonMapper, Validator validator) {
        this.planReader = jsonMapper
                .readerFor(CreateTrainingPlanRequest.class)
                .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .with(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .without(DeserializationFeature.ACCEPT_FLOAT_AS_INT);

        this.validator = validator;
    }

    public CreateTrainingPlanRequest parse(String json, GenerateTrainingPlanRequest expectedRequest
    ) {
        if (expectedRequest == null) {
            throw new IllegalArgumentException("Generation request is required.");
        }

        if (json == null || json.isBlank()) {
            throw new TextGenerationException("Ollama returned no training-plan JSON.");
        }

        CreateTrainingPlanRequest plan;

        try {
            plan = planReader.readValue(json);
        } catch (JacksonException exception) {
            throw new TextGenerationException("Ollama returned invalid training-plan JSON.", exception);
        }

        if (plan == null) {
            throw new TextGenerationException("Ollama returned a null training plan.");
        }

        validateFields(plan);
        validateRequestedFields(plan, expectedRequest);

        return plan;
    }

    private void validateFields(CreateTrainingPlanRequest plan) {
        Set<ConstraintViolation<CreateTrainingPlanRequest>> violations = validator.validate(plan);

        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(violation ->
                            violation.getPropertyPath()
                                    + ": "
                                    + violation.getMessage()
                    )
                    .sorted()
                    .collect(Collectors.joining("; "));

            throw new TextGenerationException("Generated training plan failed validation: " + details);
        }
    }

    private void validateRequestedFields(CreateTrainingPlanRequest plan, GenerateTrainingPlanRequest expectedRequest
    ) {
        if (!plan.name().equals(expectedRequest.name()) || !plan.startDate().equals(expectedRequest.startDate()) || !plan.endDate().equals(expectedRequest.endDate())) {
            throw new TextGenerationException("Generated plan changed the requested name or dates.");
        }
    }
}