package com.dando.pacepilot.assistant.api;

import com.dando.pacepilot.assistant.TrainingAnswer;
import com.dando.pacepilot.assistant.TrainingAnswerSource;
import com.dando.pacepilot.assistant.TrainingAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingAssistantController.class)
class TrainingAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingAssistantService assistantService;

    @Test
    void returnsGeneratedAnswerAndSources() throws Exception {
        UUID profileId = UUID.randomUUID();

        String question = "How often should a beginner runner rest?";

        TrainingAnswerSource source =
                new TrainingAnswerSource(
                        1,
                        "running-chunk-1",
                        "Beginner Running Foundations",
                        "Recovery between runs",
                        "knowledge/"
                                + "beginner-running-foundations.md",
                        "Beginners should recover between "
                                + "running sessions.",
                        0.91
                );

        TrainingAnswer trainingAnswer =
                new TrainingAnswer(
                        question,
                        "Beginners should include recovery between running sessions [1].",
                        List.of(source)
                );

        when(assistantService.ask(profileId, question)).thenReturn(trainingAnswer);

        mockMvc.perform(
                        post("/api/assistant/ask")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "athleteProfileId": "%s",
                                          "question": "How often should a beginner runner rest?"
                                        }
                                        """.formatted(profileId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value(question))
                .andExpect(jsonPath("$.answer")
                                .value("Beginners should include recovery between running sessions [1]."))
                .andExpect(jsonPath("$.sources.length()").value(1))
                .andExpect(jsonPath("$.sources[0].citationNumber").value(1))
                .andExpect(jsonPath("$.sources[0].chunkId").value("running-chunk-1"))
                .andExpect(jsonPath("$.sources[0].sectionTitle").value("Recovery between runs"))
                .andExpect(jsonPath("$.sources[0].similarity").value(0.91));

        verify(assistantService).ask(profileId, question);
    }

    @Test
    void rejectsBlankQuestion() throws Exception {
        UUID profileId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/assistant/ask")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "athleteProfileId": "%s",
                                          "question": " "
                                        }
                                        """.formatted(profileId))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assistantService);
    }

    @Test
    void rejectsQuestionLongerThanMaximum() throws Exception {
        UUID profileId = UUID.randomUUID();
        String longQuestion = "a".repeat(501);

        String requestBody = """
                {
                  "athleteProfileId": "%s",
                  "question": "%s"
                }
                """.formatted(profileId, longQuestion);

        mockMvc.perform(
                        post("/api/assistant/ask")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assistantService);
    }

    @Test
    void rejectsMissingAthleteProfileId() throws Exception {

        mockMvc.perform(
                        post("/api/assistant/ask")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "question": "How often should I run?"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assistantService);
    }
}