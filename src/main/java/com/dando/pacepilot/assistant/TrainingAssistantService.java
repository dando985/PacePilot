package com.dando.pacepilot.assistant;

import com.dando.pacepilot.generation.TextGenerationProvider;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrainingAssistantService {

    private static final int RETRIEVAL_LIMIT = 3;

    private static final String NO_KNOWLEDGE_MESSAGE =
            "I could not find enough information in the PacePilot training knowledge to answer that question.";

    private final SemanticRetriever semanticRetriever;
    private final TextGenerationProvider generationProvider;
    private final TrainingAssistantPromptBuilder promptBuilder;

    public TrainingAssistantService(
            SemanticRetriever semanticRetriever,
            TextGenerationProvider generationProvider,
            TrainingAssistantPromptBuilder promptBuilder
    ) {
        this.semanticRetriever = semanticRetriever;
        this.generationProvider = generationProvider;
        this.promptBuilder = promptBuilder;
    }

    public TrainingAnswer ask(String question) {
        validateQuestion(question);

        // Perform semantic retrieval
        List<SemanticSearchResult> results = semanticRetriever.search(question, RETRIEVAL_LIMIT);

        if (results.isEmpty()) {
            return new TrainingAnswer(question, NO_KNOWLEDGE_MESSAGE, List.of());
        }

        String systemMessage = promptBuilder.buildSystemMessage();

        // Insert results from retrieval into a prompt
        String userMessage = promptBuilder.buildUserMessage(question, results);

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

    private void validateQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank.");
        }
    }
}