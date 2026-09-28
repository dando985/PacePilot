package com.dando.pacepilot.assistant;

import com.dando.pacepilot.athlete.service.AthleteProfileService;
import com.dando.pacepilot.generation.TextGenerationProvider;
import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingAssistantServiceTest {

    @Mock
    private AthleteProfileService profileService;

    @Mock
    private SemanticRetriever semanticRetriever;

    @Mock
    private TextGenerationProvider generationProvider;

    private TrainingAssistantService assistantService;

    @BeforeEach
    void setUp() {
        assistantService = new TrainingAssistantService(
                profileService,
                semanticRetriever,
                generationProvider,
                new TrainingAssistantPromptBuilder()
        );
    }

    @Test
    void generatesAnswerUsingRetrievedKnowledge() {
        UUID profileId = UUID.randomUUID();
        AthleteProfile profile = createProfile(profileId);

        when(profileService.getProfileById(profileId)).thenReturn(profile);

        String question = "How often should a beginner runner rest?";

        TrainingChunk recoveryChunk = createChunk(
                "running-chunk-1",
                "Recovery between runs",
                "Beginners should include recovery between running sessions.",
                1
        );

        TrainingChunk workloadChunk = createChunk(
                "running-chunk-2",
                "Increasing workload",
                "Training workload should increase gradually.",
                2
        );

        List<SemanticSearchResult> results = List.of(
                new SemanticSearchResult(
                        recoveryChunk,
                        0.92
                ),
                new SemanticSearchResult(
                        workloadChunk,
                        0.78
                )
        );

        when(semanticRetriever.search(question, 3)).thenReturn(results);

        when(generationProvider.generate(anyString(), anyString()))
                .thenReturn("Beginners should allow recovery between running sessions [1].");

        TrainingAnswer answer = assistantService.ask(profileId, question);

        assertThat(answer.question()).isEqualTo(question);

        assertThat(answer.answer())
                .isEqualTo("Beginners should allow recovery between running sessions [1].");

        assertThat(answer.sources()).hasSize(2);

        assertThat(
                answer.sources()
                        .get(0)
                        .citationNumber()
        ).isEqualTo(1);

        assertThat(
                answer.sources()
                        .get(0)
                        .sectionTitle()
        ).isEqualTo("Recovery between runs");

        assertThat(
                answer.sources()
                        .get(1)
                        .citationNumber()
        ).isEqualTo(2);

        ArgumentCaptor<String> systemMessageCaptor = ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> userMessageCaptor = ArgumentCaptor.forClass(String.class);

        verify(generationProvider).generate(systemMessageCaptor.capture(), userMessageCaptor.capture());

        assertThat(systemMessageCaptor.getValue())
                .contains(
                        "using only the training knowledge",
                        "Do not invent sources"
                );

        assertThat(userMessageCaptor.getValue())
                .contains(
                        "Dan",
                        "BEGINNER",
                        "4",
                        "15.0 MILES",
                        "MARATHON",
                        "240 minutes",
                        question,
                        "[1]",
                        "Recovery between runs"
                );

        verify(semanticRetriever).search(question, 3);
    }

    @Test
    void doesNotGenerateAnswerWhenNoKnowledgeWasFound() {
        UUID profileId = UUID.randomUUID();
        AthleteProfile profile = createProfile(profileId);

        when(profileService.getProfileById(profileId)).thenReturn(profile);

        String question = "What should I eat during an ultramarathon?";

        when(semanticRetriever.search(question, 3)).thenReturn(List.of());

        TrainingAnswer answer = assistantService.ask(profileId, question);

        assertThat(answer.answer()).contains("could not find enough information");
        assertThat(answer.sources()).isEmpty();

        verifyNoInteractions(generationProvider);
    }

    @Test
    void rejectsBlankQuestion() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> assistantService.ask(UUID.randomUUID()," "))
                .withMessage("Question must not be blank.");

        verifyNoInteractions(profileService, semanticRetriever, generationProvider);
    }

    private TrainingChunk createChunk(
            String id,
            String sectionTitle,
            String content,
            int chunkNumber
    ) {
        return new TrainingChunk(
                id,
                "beginner-running-document",
                "Beginner Running Foundations",
                "knowledge/beginner-running-foundations.md",
                sectionTitle,
                chunkNumber,
                content
        );
    }

    // helper to create stub profile
    private AthleteProfile createProfile(UUID id) {
        FitnessGoal goal = new FitnessGoal(
                GoalType.MARATHON,
                LocalDate.of(2099, 1, 1),
                240
        );

        return new AthleteProfile(
                id,
                "Dan",
                ExperienceLevel.BEGINNER,
                4,
                15.0,
                DistanceUnit.MILES,
                goal
        );
    }
}