package com.dando.pacepilot.trainingplan.generation;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.generation.StructuredGenerationProvider;
import com.dando.pacepilot.generation.TextGenerationException;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.trainingplan.api.CreatePlannedWorkoutRequest;
import com.dando.pacepilot.trainingplan.api.CreateTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.api.CreateTrainingWeekRequest;
import com.dando.pacepilot.trainingplan.api.GenerateTrainingPlanRequest;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import com.dando.pacepilot.trainingplan.exception.InvalidTrainingPlanException;
import com.dando.pacepilot.trainingplan.generation.TrainingPlanGenerationParser.GeneratedWorkout;
import com.dando.pacepilot.trainingplan.generation.TrainingPlanGenerationParser.TargetKind;
import com.dando.pacepilot.trainingplan.service.TrainingPlanService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class TrainingPlanGenerationService {

    private static final int RETRIEVAL_LIMIT = 3;
    private static final int MAX_ATTEMPTS = 2;
    private static final int MAX_PLAN_DAYS = 12 * 7;
    private static final int MAX_DURATION_MINUTES = 360;
    private static final int MAX_DISTANCE_METERS = 50_000;

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

        int planDays = validateRequest(request);
        AthleteProfile profile = profileService.getProfileById(athleteId);
        int availableDays = request.availableTrainingDaysPerWeek();

        // build the list of training dates based on the plan's start date, end date, and available training days
        List<LocalDate> trainingDates = buildTrainingDates(request.startDate(), planDays, availableDays);

        // retrieve relevant training knowledge for the athlete and request
        List<SemanticSearchResult> results = semanticRetriever.search(buildRetrievalQuery(profile, request), RETRIEVAL_LIMIT);

        if (results.isEmpty()) {
            throw new TextGenerationException("No relevant training knowledge was found for this athlete.");
        }

        List<GeneratedWorkout> generatedWorkouts = generateWorkouts(request, profile, trainingDates, results);

        CreateTrainingPlanRequest plan = buildPlan(request, planDays, trainingDates, generatedWorkouts);

        try {
            return planService.createPlan(athleteId, plan);
        } catch (InvalidTrainingPlanException exception) {
            throw new TextGenerationException("Generated plan failed domain validation: " + exception.getMessage(), exception);
        }
    }

    private int validateRequest(GenerateTrainingPlanRequest request) {
        if (request.name() == null
                || request.name().isBlank()
                || request.startDate() == null
                || request.endDate() == null
                || request.endDate().isBefore(request.startDate())) {
            throw invalidRequest("A plan name and valid date range are required.");
        }

        if (request.goal() == null
                || request.goal().type() == null
                || request.goal().targetDate() == null) {
            throw invalidRequest("A goal with a type and target date is required.");
        }

        if (request.goal().targetTimeMinutes() != null && request.goal().targetTimeMinutes() <= 0) {
            throw invalidRequest("Goal target time must be greater than zero.");
        }

        if (!request.isGoalDateValid()) {
            throw invalidRequest("Goal target date cannot be before the plan's start date.");
        }

        Integer availableDays = request.availableTrainingDaysPerWeek();

        if (availableDays == null || availableDays < 1 || availableDays > 7) {
            throw invalidRequest("Available training days must be between 1 and 7.");
        }

        if (request.distanceUnit() == null) {
            throw invalidRequest("Plan distance unit is required.");
        }

        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;

        if (days > MAX_PLAN_DAYS) {
            throw invalidRequest("Generate at most 12 weeks (84 days) per request.");
        }

        LocalDate lastWeekStart = request.startDate().plusDays((days - 1) / 7 * 7);

        if (lastWeekStart.isAfter(LocalDate.MAX.minusDays(6))) {
            throw invalidRequest("Plan dates exceed the supported calendar range.");
        }

        return (int) days;
    }

    private InvalidTrainingPlanException invalidRequest(String message) {
        return new InvalidTrainingPlanException(new IllegalArgumentException(message));
    }

    private List<LocalDate> buildTrainingDates(LocalDate startDate, int planDays, int availableDays) {
        boolean[] activeDays = new boolean[7];

        for (int session = 0; session < availableDays; session++) {
            int day = (int) Math.round(session * 7.0 / availableDays);
            activeDays[day] = true;
        }

        List<LocalDate> dates = new ArrayList<>();

        for (int offset = 0; offset < planDays; offset++) {
            if (activeDays[offset % 7]) {
                dates.add(startDate.plusDays(offset));
            }
        }

        return List.copyOf(dates);
    }

    private List<GeneratedWorkout> generateWorkouts(
            GenerateTrainingPlanRequest request,
            AthleteProfile profile,
            List<LocalDate> trainingDates,
            List<SemanticSearchResult> results
    ) {
        Map<String, Object> schema = schemaBuilder.schema(trainingDates.size(), MAX_DURATION_MINUTES, MAX_DISTANCE_METERS);

        String systemMessage = promptBuilder.buildWorkoutSystemMessage();
        String feedback = null;
        TextGenerationException lastFailure = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String userMessage = promptBuilder.buildWorkoutUserMessage(
                    request,
                    profile,
                    trainingDates,
                    results,
                    schema,
                    feedback
            );

            String generatedJson = generationProvider.generate(systemMessage, userMessage, schema);

            try {
                return parser.parseWorkouts(generatedJson, trainingDates.size(), MAX_DURATION_MINUTES, MAX_DISTANCE_METERS);
            } catch (TextGenerationException exception) {
                lastFailure = exception;
                feedback = exception.getMessage();
            }
        }

        throw new TextGenerationException(
                "Unable to generate valid workouts after %d attempts: %s".formatted(MAX_ATTEMPTS, feedback), lastFailure
        );
    }

    private CreateTrainingPlanRequest buildPlan(
            GenerateTrainingPlanRequest request,
            int planDays,
            List<LocalDate> trainingDates,
            List<GeneratedWorkout> generatedWorkouts
    ) {
        List<CreateTrainingWeekRequest> weeks = new ArrayList<>();
        int workoutIndex = 0;

        for (int weekOffset = 0; weekOffset < planDays; weekOffset += 7) {
            List<CreatePlannedWorkoutRequest> workouts = new ArrayList<>();

            for (int offset = weekOffset; offset < Math.min(weekOffset + 7, planDays); offset++) {

                LocalDate date = request.startDate().plusDays(offset);

                if (workoutIndex < trainingDates.size() && date.equals(trainingDates.get(workoutIndex))) {
                    workouts.add(buildWorkout(date, generatedWorkouts.get(workoutIndex++), request.distanceUnit()));
                } else {
                    workouts.add(new CreatePlannedWorkoutRequest(
                            date,
                            WorkoutType.REST,
                            "Rest Day",
                            null,
                            null,
                            null,
                            "Rest today. No workout is scheduled."
                    ));
                }
            }

            weeks.add(new CreateTrainingWeekRequest(weekOffset / 7 + 1, request.startDate().plusDays(weekOffset), List.copyOf(workouts)));
        }

        return new CreateTrainingPlanRequest(
                request.name(),
                request.startDate(),
                request.endDate(),
                request.goal(),
                request.availableTrainingDaysPerWeek(),
                request.distanceUnit(),
                List.copyOf(weeks)
        );
    }

    private CreatePlannedWorkoutRequest buildWorkout(LocalDate date, GeneratedWorkout workout, DistanceUnit preferredUnit) {
        Double distance = null;
        DistanceUnit unit = null;
        Integer duration = null;
        String target;

        if (workout.targetKind() == TargetKind.DURATION_MINUTES) {
            duration = workout.targetValue();
            target = duration + (duration == 1 ? " minute" : " minutes");
        } else {
            unit = preferredUnit;

            BigDecimal metersPerUnit = new BigDecimal(unit == DistanceUnit.MILES ? "1609.344" : "1000");

            BigDecimal converted = BigDecimal.valueOf(workout.targetValue())
                    .divide(metersPerUnit, 6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();

            distance = converted.doubleValue();

            target = converted.toPlainString() + (unit == DistanceUnit.MILES ? " mi" : " km");
        }

        String title = switch (workout.type()) {
            case WALK -> "Walk";
            case EASY_RUN -> "Easy Run";
            case RECOVERY_RUN -> "Recovery Run";
            case LONG_RUN -> "Long Run";
            case TEMPO_RUN -> "Tempo Run";
            case INTERVALS -> "Intervals";
            case STRENGTH -> "Strength Training";
            case CROSS_TRAINING -> "Cross Training";
            case RACE -> "Race";
            case REST -> throw new IllegalArgumentException("REST workouts are created separately.");
        };

        String guidance = switch (workout.type()) {
            case WALK ->
                    "Walk at a comfortable effort.";
            case EASY_RUN ->
                    "Run at an easy, conversational effort.";
            case RECOVERY_RUN ->
                    "Run gently and prioritize recovery.";
            case LONG_RUN ->
                    "Keep the run comfortable and controlled.";
            case TEMPO_RUN ->
                    "Use a controlled, comfortably hard effort for the main portion.";
            case INTERVALS ->
                    "Alternate faster running and easy recovery within the total target.";
            case STRENGTH ->
                    "Use controlled strength exercises, including rests within the total target.";
            case CROSS_TRAINING ->
                    "Choose a non-running activity at a comfortable effort.";
            case RACE ->
                    "Complete the event at an effort appropriate to your preparation.";
            case REST -> throw new IllegalArgumentException(
                    "REST workouts are created separately."
            );
        };

        String instructions = title + ": " + target + ". " + guidance + " Include any warm-up and cooldown within this total.";

        String coachingComment = workout.coachingComment();

        if (!coachingComment.isBlank()) {
            instructions += "\n\nCoach's note: " + coachingComment.strip();
        }

        return new CreatePlannedWorkoutRequest(
                date,
                workout.type(),
                title,
                distance,
                unit,
                duration,
                instructions
        );
    }

    private String buildRetrievalQuery(AthleteProfile profile, GenerateTrainingPlanRequest request) {
        return """
            %s training plan for a %s athlete with %d training days per week.
            Weekly workout scheduling, recovery, and gradual progression.
            """.formatted(
                readableName(request.goal().type()),
                readableName(profile.experienceLevel()),
                request.availableTrainingDaysPerWeek()
        ).trim();
    }

    // private helper to convert enum values to readable strings for prompt generation
    private String readableName(Enum<?> value) {
        if (value == null) {
            return "unspecified";
        }

        return value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}