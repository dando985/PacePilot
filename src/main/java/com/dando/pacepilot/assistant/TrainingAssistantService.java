package com.dando.pacepilot.assistant;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.generation.TextGenerationProvider;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.service.TrainingPlanService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingAssistantService {

    private static final int RETRIEVAL_LIMIT = 3;

    private static final String NO_KNOWLEDGE_MESSAGE =
            "I could not find enough information in the PacePilot training knowledge to answer that question.";

    private final AthleteProfileService profileService;
    private final TrainingPlanService planService;
    private final SemanticRetriever semanticRetriever;
    private final TextGenerationProvider generationProvider;
    private final TrainingAssistantPromptBuilder promptBuilder;

    public TrainingAssistantService(
            AthleteProfileService profileService,
            TrainingPlanService planService,
            SemanticRetriever semanticRetriever,
            TextGenerationProvider generationProvider,
            TrainingAssistantPromptBuilder promptBuilder
    ) {
        this.profileService = profileService;
        this.planService = planService;
        this.semanticRetriever = semanticRetriever;
        this.generationProvider = generationProvider;
        this.promptBuilder = promptBuilder;
    }

    public TrainingAnswer ask(UUID athleteProfileId, String question, UUID trainingPlanId) {
        validateQuestion(athleteProfileId, question);

        // retrieve the athlete profile and training plan (if provided)
        AthleteProfile profile = profileService.getProfileById(athleteProfileId);

        TrainingPlan plan = null;

        if (trainingPlanId != null) {
            plan = planService.getPlanById(trainingPlanId);

            if (!plan.athleteProfileId().equals(athleteProfileId)) {
                throw new IllegalArgumentException("Selected training plan must belong to the athlete.");
            }
        }

        // retrieve relevant knowledge passages from the semantic retriever
        List<SemanticSearchResult> results = semanticRetriever.search(question, RETRIEVAL_LIMIT);

        if (results.isEmpty()) {
            return new TrainingAnswer(
                    question,
                    NO_KNOWLEDGE_MESSAGE,
                    List.of()
            );
        }

        // build the system prompt for the LLM
        String systemMessage = promptBuilder.buildSystemMessage();

        // build the user prompt for the LLM
        String userMessage = promptBuilder.buildUserMessage(question, profile, plan, results);

        // generate the answer using the LLM
        String generatedAnswer = generationProvider.generate(systemMessage, userMessage);

        List<TrainingAnswerSource> sources = createSources(results);

        return new TrainingAnswer(question, generatedAnswer, sources);
    }

    // private helper to convert the semantic search results into answer sources
    private List<TrainingAnswerSource> createSources(List<SemanticSearchResult> results) {
        List<TrainingAnswerSource> sources = new ArrayList<>();

        for (int index = 0; index < results.size(); index++) {
            sources.add(TrainingAnswerSource.from(index + 1, results.get(index)));
        }

        return List.copyOf(sources);
    }

    // private helper to validate the question and athlete profile ID
    private void validateQuestion(UUID athleteProfileId, String question) {
        if (athleteProfileId == null) {
            throw new IllegalArgumentException("Athlete profile ID must not be null.");
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank.");
        }
    }
}