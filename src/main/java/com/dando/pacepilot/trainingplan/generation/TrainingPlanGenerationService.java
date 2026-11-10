package com.dando.pacepilot.trainingplan.generation;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.generation.StructuredGenerationProvider;
import com.dando.pacepilot.generation.TextGenerationException;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import com.dando.pacepilot.trainingplan.api.CreatePlannedWorkoutRequest;
import com.dando.pacepilot.trainingplan.api.CreateTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.api.CreateTrainingWeekRequest;
import com.dando.pacepilot.trainingplan.api.GenerateTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import com.dando.pacepilot.trainingplan.exception.InvalidTrainingPlanException;
import com.dando.pacepilot.trainingplan.service.TrainingPlanService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

// Service responsible for generating training plans based on athlete profiles, semantic search results, and structured generation prompts.
@Service
public class TrainingPlanGenerationService {

    private static final int RETRIEVAL_LIMIT = 3;

    private final AthleteProfileService profileService;
    private final SemanticRetriever semanticRetriever;
    private final StructuredGenerationProvider generationProvider;
    private final TrainingPlanJsonSchema schemaBuilder;
    private final TrainingPlanGenerationPromptBuilder promptBuilder;
    private final TrainingPlanGenerationParser parser;
    private final TrainingPlanService planService;

    public TrainingPlanGenerationService(
            AthleteProfileService profileService,
            SemanticRetriever semanticRetriever,
            StructuredGenerationProvider generationProvider,
            TrainingPlanJsonSchema schemaBuilder,
            TrainingPlanGenerationPromptBuilder promptBuilder,
            TrainingPlanGenerationParser parser,
            TrainingPlanService planService
    ) {
        this.profileService = profileService;
        this.semanticRetriever = semanticRetriever;
        this.generationProvider = generationProvider;
        this.schemaBuilder = schemaBuilder;
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.planService = planService;
    }

    public TrainingPlan generatePlan(UUID athleteId, GenerateTrainingPlanRequest request) {
        if (athleteId == null || request == null) {
            throw new IllegalArgumentException("Athlete ID and generation request are required.");
        }

        AthleteProfile profile = profileService.getProfileById(athleteId);

        List<SemanticSearchResult> results = semanticRetriever.search(buildRetrievalQuery(profile), RETRIEVAL_LIMIT);

        if (results.isEmpty()) {
            throw new TextGenerationException("No relevant training knowledge was found for this athlete.");
        }

        // Build the JSON schema, system message, and user message for the structured generation prompt.
        Map<String, Object> schema = schemaBuilder.schema();
        String systemMessage = promptBuilder.buildSystemMessage();
        String userMessage = promptBuilder.buildUserMessage(request, profile, results, schema);
        String generatedJson = generationProvider.generate(systemMessage, userMessage, schema);

        // Parse the generated JSON into a CreateTrainingPlanRequest object and validate the generated schedule against the athlete's available training days.
        CreateTrainingPlanRequest generatedPlan = parser.parse(generatedJson, request);

        // Validate the generated schedule to ensure it adheres to the athlete's available training days per week and that workouts are ordered correctly.
        validateGeneratedSchedule(generatedPlan, profile.availableTrainingDaysPerWeek());

        try {
            return planService.createPlan(athleteId, generatedPlan);
        } catch (InvalidTrainingPlanException exception) {
            throw new TextGenerationException("Generated plan failed domain validation: " + exception.getMessage(), exception);
        }
    }

    // Private helper method to build a retrieval query based on the athlete's profile, including goal type, experience level, and available training days per week.
    private String buildRetrievalQuery(AthleteProfile profile) {
        return """
                %s training plan for a %s athlete with %d training days per week.
                Weekly workout scheduling, recovery, and gradual progression.
                """.formatted(
                readableName(profile.goal().type()),
                readableName(profile.experienceLevel()),
                profile.availableTrainingDaysPerWeek()
        ).trim();
    }

    // Private helper method to convert enum values to a more readable format for inclusion in the retrieval query.
    private String readableName(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    // Private helper method to validate the generated training plan schedule against the athlete's available training days per week and ensure that workouts are ordered correctly.
    private void validateGeneratedSchedule(CreateTrainingPlanRequest plan, int availableDays) {
        for (CreateTrainingWeekRequest week : plan.weeks()) {

            // Count the number of distinct training days (non-REST workouts) in the week and ensure it does not exceed the athlete's available training days per week.
            long trainingDays = week.workouts().stream()
                    .filter(workout -> workout.type() != WorkoutType.REST)
                    .map(CreatePlannedWorkoutRequest::scheduledDate)
                    .distinct()
                    .count();

            if (trainingDays > availableDays) {
                throw new TextGenerationException(
                        "Generated week %d exceeds the athlete's %d available training days."
                                .formatted(week.weekNumber(), availableDays)
                );
            }

            LocalDate previousDate = null;

            for (CreatePlannedWorkoutRequest workout : week.workouts()) {
                LocalDate date = workout.scheduledDate();

                if (previousDate != null && date.isBefore(previousDate)) {
                    throw new TextGenerationException("Generated workouts are not ordered by date.");
                }

                previousDate = date;

                if (workout.type() == WorkoutType.REST &&
                        (workout.targetDistance() != null || workout.distanceUnit() != null || workout.targetDurationMinutes() != null)) {
                    throw new TextGenerationException("Generated REST workout contains workout targets.");
                }
            }
        }
    }
}