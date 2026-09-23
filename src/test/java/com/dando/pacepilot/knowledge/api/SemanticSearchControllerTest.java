package com.dando.pacepilot.knowledge.api;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SemanticSearchController.class)
class SemanticSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SemanticRetriever semanticRetriever;

    @Test
    void returnsSemanticSearchResults() throws Exception {
        String query = "How often should a beginner runner rest?";

        TrainingChunk chunk = new TrainingChunk(
                "running-chunk-1",
                "running-document",
                "Beginner Running Foundations",
                "knowledge/beginner-running-foundations.md",
                "Recovery between runs",
                1,
                "Beginners should recover between running sessions."
        );

        SemanticSearchResult searchResult = new SemanticSearchResult(chunk, 0.91);

        when(semanticRetriever.search(query, 2)).thenReturn(List.of(searchResult));

        mockMvc.perform(
                        post("/api/knowledge/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "query": "How often should a beginner runner rest?",
                                          "limit": 2
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value(query))
                .andExpect(jsonPath("$.resultCount").value(1))
                .andExpect(jsonPath("$.results[0].chunkId").value("running-chunk-1"))
                .andExpect(jsonPath("$.results[0].documentTitle").value("Beginner Running Foundations"))
                .andExpect(jsonPath("$.results[0].sectionTitle").value("Recovery between runs"))
                .andExpect(jsonPath("$.results[0].similarity").value(0.91));

        verify(semanticRetriever).search(query, 2);
    }

    @Test
    void usesDefaultLimitWhenLimitIsMissing() throws Exception {

        String query = "beginner running";

        when(semanticRetriever.search(query, 3)).thenReturn(List.of());

        mockMvc.perform(
                        post("/api/knowledge/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "query": "beginner running"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount").value(0))
                .andExpect(jsonPath("$.results").isArray());

        verify(semanticRetriever).search(query, 3);
    }

    @Test
    void rejectsBlankQuery() throws Exception {
        mockMvc.perform(
                        post("/api/knowledge/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "query": " ",
                                          "limit": 3
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(semanticRetriever);
    }

    @Test
    void rejectsLimitAboveMaximum() throws Exception {
        mockMvc.perform(
                        post("/api/knowledge/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "query": "recovery",
                                          "limit": 20
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(semanticRetriever);
    }
}