package com.dando.pacepilot.assistant;

import com.dando.pacepilot.generation.TextGenerationProvider;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingAssistantService {

    private static final int RETRIEVAL_LIMIT = 3;
    private final AthleteProfileService profileService;

    private static final String NO_KNOWLEDGE_MESSAGE =
            "I could not find enough information in the PacePilot training knowledge to answer that question.";

    private final SemanticRetriever semanticRetriever;
    private final TextGenerationProvider generationProvider;
    private final TrainingAssistantPromptBuilder promptBuilder;

    public TrainingAssistantService(
            AthleteProfileService profileService,
            SemanticRetriever semanticRetriever,
            TextGenerationProvider generationProvider,
            TrainingAssistantPromptBuilder promptBuilder
    ) {
        this.profileService = profileService;
        this.semanticRetriever = semanticRetriever;
        this.generationProvider = generationProvider;
        this.promptBuilder = promptBuilder;
    }

    public TrainingAnswer ask(UUID athletePofileID, String question) {
        validateQuestion(athletePofileID, question);

        AthleteProfile profile = profileService.getProfileById(athletePofileID);

        // Perform semantic retrieval
        List<SemanticSearchResult> results = semanticRetriever.search(question, RETRIEVAL_LIMIT);

        if (results.isEmpty()) {
            return new TrainingAnswer(question, NO_KNOWLEDGE_MESSAGE, List.of());
        }

        // Establish model's rules
        String systemMessage = promptBuilder.buildSystemMessage();

        // Insert results from retrieval into a prompt
        String userMessage = promptBuilder.buildUserMessage(question, profile, results);

        // Get LLM's generated response
        String generatedAnswer = generationProvider.generate(systemMessage, userMessage);

        List<TrainingAnswerSource> sources = createSources(results);

        return new TrainingAnswer(question, generatedAnswer, sources);
    }

    private List<TrainingAnswerSource> createSources(List<SemanticSearchResult> results) {
        List<TrainingAnswerSource> sources = new ArrayList<>();

        for (int index = 0; index < results.size(); index++) {
            sources.add(TrainingAnswerSource.from(index + 1, results.get(index)));
        }

        return List.copyOf(sources);
    }

    private void validateQuestion(UUID athleteProfileId, String question) {
        if (athleteProfileId == null) {
            throw new IllegalArgumentException("Athlete profile ID must not be null.");
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank.");
        }
    }
}